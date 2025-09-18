package com.nextera.fim.app.model;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;        // Add this import
import java.time.LocalDate;        // Add this import

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuaranteeInstrumentDTO {
    private String gtiSystemId;
    private String guarantorName;
    private String applicantName;
    private String beneficiaryName;
    private String applicantQuickCode;
    private String beneficiaryQuickCode;
    private String guarantorQuickCode;
    private String category;
    private String nominalCurrency;
    private BigDecimal actualAmount;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private String guaranteeStatus;
    private String amdSystemId;
    private LocalDate amendmentDate;
    private LocalDate newExpiryDate;
    private String amendmentStatus;
    private String guaranteeTypeDetails;
    private String internalReference;
    private BigDecimal newNominalAmount;
    private BigDecimal nominalAmountChange;
    private String automaticExtensionPeriod;
    private String bankReferenceNumber;
    private Boolean treasuryFlag;
    private String instrumentType;
    private String businessGroup;
    private LocalDate releaseDate;
    private String otherComments;
    private String ratingTrigger;
    private Boolean separateAuthority;
    private Boolean consolidated;
    private String gtyDisclosure1;
    private String gtyDisclosure2Subcategory;
    private String lcDisclosure1;
    private String lcDisclosure2Subcategory;
    private String contractNumber;
}