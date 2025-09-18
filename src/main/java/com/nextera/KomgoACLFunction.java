package com.nextera.fim.app.ingest;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification;
import com.nextera.GuaranteeInstrumentCsvMapper;
import com.nextera.fim.app.model.GuaranteeInstrumentDTO;
import com.nextera.fim.app.model.BasePublishableEvent;
import com.nextera.fim.app.model.StreamId;
import com.nextera.fim.app.MercuriusFacade;
import com.nextera.fim.app.MercuriusFacadeImpl;
import com.nextera.fim.app.Cause;
import com.nextera.fim.app.UnknownCause;
import com.opencsv.CSVReader;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReaderBuilder;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemResponse;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lambda handler that reads CSVs from S3, maps rows to GuaranteeInstrumentDTOs,
 * and publishes each as a JSON message to SQS. Also triggers a CollateralChangedEvent.
 * You can scan the DynamoDB to check stored items.
 *
 * Environment:
 *   - QUEUE_URL : SQS queue to send messages
 * CSV dialect:
 *   - comma separated, UTF-8, with header row
 */
public class KomgoACLFunction implements RequestHandler<S3Event, String> {

    private static final Logger logger = LoggerFactory.getLogger(KomgoACLFunction.class);
    private final S3Client s3 = S3Client.create();
    private final SqsClient sqs = SqsClient.create();
    private final DynamoDbClient dynamoDbClient = DynamoDbClient.create();
    private final ObjectMapper mapper = new ObjectMapper();
    private final GuaranteeInstrumentCsvMapper rowMapper = new GuaranteeInstrumentCsvMapper();
    
    // Declare and initialize MercuriusFacade
    private final MercuriusFacade mercuriusFacade = new MercuriusFacadeImpl();

    @Override
    public String handleRequest(S3Event event, Context context) {
        String queueUrl = System.getenv("QUEUE_URL");
        if (queueUrl == null || queueUrl.isBlank()) {
            logger.error("QUEUE_URL environment variable is required but not set.");
            throw new IllegalStateException("QUEUE_URL env var is required");
        }

        List<S3EventNotification.S3EventNotificationRecord> records = event.getRecords();
        if (records == null || records.isEmpty()) {
            logger.warn("No records found in the S3 event.");
            return "No records";
        }

        for (S3EventNotification.S3EventNotificationRecord record : records) {
            String bucket = record.getS3().getBucket().getName();
            String key = record.getS3().getObject().getKey();
            logger.info("Processing object from bucket: {}, key: {}", bucket, key);
            processObject(bucket, key, queueUrl, context);
        }

        // Optional: Execute a query on the DynamoDB table
        queryItems("guranteedata", "systemid", "unique-id-123");

        return "OK";
    }

    private void processObject(String bucket, String key, String queueUrl, Context ctx) {
        GetObjectRequest req = GetObjectRequest.builder().bucket(bucket).key(key).build();
        try (InputStream in = s3.getObject(req);
             InputStreamReader isr = new InputStreamReader(in, StandardCharsets.UTF_8)) {

            CSVReader reader = new CSVReaderBuilder(isr)
                    .withCSVParser(new CSVParserBuilder().withSeparator(',').build())
                    .build();

            String[] header = reader.readNext();
            if (header == null) {
                logger.warn("CSV file is empty or does not contain a header row.");
                return;
            }

            String[] row;
            while ((row = reader.readNext()) != null) {
                GuaranteeInstrumentDTO dto = rowMapper.map(header, row);

                // Serializing DTO to JSON
                String body = mapper.writeValueAsString(dto);

                // Send to SQS
                sqs.sendMessage(SendMessageRequest.builder()
                        .queueUrl(queueUrl)
                        .messageBody(body)
                        .build());

                // Send to DynamoDB
                sendDataToDynamoDB(dto);

                // Create and publish the CollateralChangedEvent with additional data
                CollateralChangedEvent collateralEvent = new CollateralChangedEvent(
                    StreamId.of("Collateral", dto.getGtiSystemId()), // Assume this serves as a unique identifier
                    dto.getGtiSystemId(),
                    Instant.now(),
                    dto.getGuarantorName(),
                    dto.getActualAmount()
                );
                Cause cause = new UnknownCause();
                mercuriusFacade.publishEvent(collateralEvent, cause);
            }
        } catch (Exception e) {
            logger.error("Error processing s3://{}/{}", bucket, key, e);
            throw new RuntimeException("Failed processing s3://" + bucket + "/" + key, e);
        }
    }

    private void sendDataToDynamoDB(GuaranteeInstrumentDTO dto) {
        Map<String, AttributeValue> itemValues = new HashMap<>();

        // Ensure all necessary fields from the DTO are added to the itemValues map
        itemValues.put("systemid", nonNullStringAttr(dto.getGtiSystemId())); // Ensure that 'systemid' is always present
        itemValues.put("guarantorName", nonNullStringAttr(dto.getGuarantorName()));
        itemValues.put("applicantName", nonNullStringAttr(dto.getApplicantName()));
        itemValues.put("beneficiaryName", nonNullStringAttr(dto.getBeneficiaryName()));
        itemValues.put("applicantQuickCode", nonNullStringAttr(dto.getApplicantQuickCode()));
        itemValues.put("beneficiaryQuickCode", nonNullStringAttr(dto.getBeneficiaryQuickCode()));
        itemValues.put("guarantorQuickCode", nonNullStringAttr(dto.getGuarantorQuickCode()));
        itemValues.put("category", nonNullStringAttr(dto.getCategory()));
        itemValues.put("nominalCurrency", nonNullStringAttr(dto.getNominalCurrency()));
        addBigDecimalAttribute(itemValues, "actualAmount", dto.getActualAmount());
        addDateAttribute(itemValues, "issueDate", dto.getIssueDate());
        addDateAttribute(itemValues, "expiryDate", dto.getExpiryDate());
        itemValues.put("guaranteeStatus", nonNullStringAttr(dto.getGuaranteeStatus()));
        itemValues.put("amdSystemId", nonNullStringAttr(dto.getAmdSystemId()));
        addDateAttribute(itemValues, "amendmentDate", dto.getAmendmentDate());
        addDateAttribute(itemValues, "newExpiryDate", dto.getNewExpiryDate());
        itemValues.put("amendmentStatus", nonNullStringAttr(dto.getAmendmentStatus()));
        itemValues.put("guaranteeTypeDetails", nonNullStringAttr(dto.getGuaranteeTypeDetails()));
        itemValues.put("internalReference", nonNullStringAttr(dto.getInternalReference()));
        addBigDecimalAttribute(itemValues, "newNominalAmount", dto.getNewNominalAmount());
        addBigDecimalAttribute(itemValues, "nominalAmountChange", dto.getNominalAmountChange());
        itemValues.put("automaticExtensionPeriod", nonNullStringAttr(dto.getAutomaticExtensionPeriod()));
        itemValues.put("bankReferenceNumber", nonNullStringAttr(dto.getBankReferenceNumber()));
        addBooleanAttribute(itemValues, "treasuryFlag", dto.getTreasuryFlag());
        itemValues.put("instrumentType", nonNullStringAttr(dto.getInstrumentType()));
        itemValues.put("businessGroup", nonNullStringAttr(dto.getBusinessGroup()));
        addDateAttribute(itemValues, "releaseDate", dto.getReleaseDate());
        itemValues.put("otherComments", nonNullStringAttr(dto.getOtherComments()));
        itemValues.put("ratingTrigger", nonNullStringAttr(dto.getRatingTrigger()));
        addBooleanAttribute(itemValues, "separateAuthority", dto.getSeparateAuthority());
        addBooleanAttribute(itemValues, "consolidated", dto.getConsolidated());
        itemValues.put("gtyDisclosure1", nonNullStringAttr(dto.getGtyDisclosure1()));
        itemValues.put("gtyDisclosure2Subcategory", nonNullStringAttr(dto.getGtyDisclosure2Subcategory()));
        itemValues.put("lcDisclosure1", nonNullStringAttr(dto.getLcDisclosure1()));
        itemValues.put("lcDisclosure2Subcategory", nonNullStringAttr(dto.getLcDisclosure2Subcategory()));
        itemValues.put("contractNumber", nonNullStringAttr(dto.getContractNumber()));

        logger.debug("Prepared item values for DynamoDB: {}", itemValues);

        PutItemRequest request = PutItemRequest.builder()
                .tableName("guranteedata")  // Ensure this matches your actual table name
                .item(itemValues)
                .build();

        try {
            logger.debug("Attempting to put item into DynamoDB: {}", request);
            PutItemResponse response = dynamoDbClient.putItem(request);
            logger.info("Successfully put item into DynamoDB table: {}", response);
        } catch (Exception e) {
            logger.error("Failed to put item into DynamoDB table with exception: {}", e.getMessage());
        }
    }

    private AttributeValue nonNullStringAttr(String value) {
        return (value != null && !value.isEmpty()) ? AttributeValue.builder().s(value).build() : AttributeValue.builder().s("default-systemid").build(); // Consider a default value or handle null case appropriately
    }

    private void addStringAttribute(Map<String, AttributeValue> item, String key, String value) {
        if (value != null) {
            item.put(key, AttributeValue.builder().s(value).build());
        }
    }

    private void addBigDecimalAttribute(Map<String, AttributeValue> item, String key, BigDecimal value) {
        if (value != null) {
            item.put(key, AttributeValue.builder().n(value.toPlainString()).build());
        }
    }

    private void addDateAttribute(Map<String, AttributeValue> item, String key, LocalDate value) {
        if (value != null) {
            item.put(key, AttributeValue.builder().s(value.toString()).build());
        }
    }

    private void addBooleanAttribute(Map<String, AttributeValue> item, String key, Boolean value) {
        if (value != null) {
            item.put(key, AttributeValue.builder().bool(value).build());
        }
    }

    private void checkDynamoDBItems() {
        try {
            ScanRequest scanRequest = ScanRequest.builder()
                    .tableName("guranteedata")
                    .build();

            ScanResponse scanResponse = dynamoDbClient.scan(scanRequest);

            for (Map<String, AttributeValue> item : scanResponse.items()) {
                logger.info("DynamoDB Item: {}", item);
            }
        } catch (Exception e) {
            logger.error("Failed to scan DynamoDB items.", e);
        }
    }

    private void queryItems(String tableName, String primaryKey, String keyValue) {
        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        expressionAttributeValues.put(":value", AttributeValue.builder().s(keyValue).build());

        QueryRequest queryRequest = QueryRequest.builder()
                .tableName(tableName)
                .keyConditionExpression(primaryKey + " = :value")
                .expressionAttributeValues(expressionAttributeValues)
                .build();

        try {
            QueryResponse queryResponse = dynamoDbClient.query(queryRequest);
            for (Map<String, AttributeValue> item : queryResponse.items()) {
                logger.info("Queried DynamoDB Item: {}", item);
            }
        } catch (Exception e) {
            logger.error("Failed to query DynamoDB items.", e);
        }
    }

    public static class CollateralChangedEvent extends BasePublishableEvent {
        private String collateralKey;
        private Instant changedTime;
        private String guarantorName; // New field example
        private BigDecimal actualAmount; // New field example

        public CollateralChangedEvent(StreamId streamId, String collateralKey, Instant changedTime, String guarantorName, BigDecimal actualAmount) {
            super(streamId);
            this.collateralKey = collateralKey;
            this.changedTime = changedTime;
            this.guarantorName = guarantorName;
            this.actualAmount = actualAmount;
        }
    }
}