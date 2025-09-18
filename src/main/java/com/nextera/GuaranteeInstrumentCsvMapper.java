package com.nextera;

import com.nextera.fim.app.model.GuaranteeInstrumentDTO;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

public final class GuaranteeInstrumentCsvMapper {

    private static final List<DateTimeFormatter> DATE_FORMATS = List.of(
        DateTimeFormatter.ISO_LOCAL_DATE,
        DateTimeFormatter.ofPattern("M/d/uuuu"),
        DateTimeFormatter.ofPattern("M/d/uu"),
        DateTimeFormatter.ofPattern("d-MMM-uuuu"),
        DateTimeFormatter.ofPattern("d-MMM-uu"),
        DateTimeFormatter.ofPattern("MMM d, uuuu")
    );

    private static String norm(String s) {
        if (s == null) return "";
        String t = s.trim();
        if (t.startsWith("'")) t = t.substring(1);
        t = t.replace("(", " ").replace(")", " ");
        t = t.replace("-", " ").replace("_", " ").replace("/", " ");
        t = t.replaceAll("[^0-9A-Za-z ]+", " ");
        t = t.replaceAll("\\s+", " ").trim().toLowerCase(Locale.ROOT);
        return t;
    }

    public GuaranteeInstrumentDTO map(String[] header, String[] row) {
        Map<String, Integer> idx = new HashMap<>();
        for (int i = 0; i < header.length; i++) {
            idx.put(norm(header[i]), i);
        }

        GuaranteeInstrumentDTO.GuaranteeInstrumentDTOBuilder b = GuaranteeInstrumentDTO.builder();

        // Helper functions
        java.util.function.Function<String[], String> get = (aliases) -> {
            for (String a : aliases) {
                Integer i = idx.get(norm(a));
                if (i != null && i < row.length) {
                    String v = row[i] != null ? row[i].trim() : null;
                    if (v != null && !v.isEmpty()) return v;
                }
            }
            return null;
        };

        java.util.function.Function<String[], LocalDate> getDate = (aliases) -> {
            String v = get.apply(aliases);
            if (v == null) return null;
            for (DateTimeFormatter f : DATE_FORMATS) {
                try {
                    return LocalDate.parse(v, f);
                } catch (DateTimeParseException ignored) {}
            }
            return null;
        };

        java.util.function.Function<String[], BigDecimal> getBig = (aliases) -> {
            String v = get.apply(aliases);
            if (v == null) return null;
            String cleaned = v.replace(",", "");
            try { return new BigDecimal(cleaned); } catch (Exception e) { return null; }
        };

        java.util.function.Function<String[], Boolean> getBool = (aliases) -> {
            String v = get.apply(aliases);
            if (v == null) return null;
            switch (v.trim().toLowerCase(Locale.ROOT)) {
                case "y": case "yes": case "true": case "1": case "t": return true;
                case "n": case "no": case "false": case "0": case "f": return false;
                default: return null;
            }
        };

        // Map fields
        b.gtiSystemId(get.apply(new String[]{"(GTI) System ID"}));
        b.guarantorName(get.apply(new String[]{"(GTI) Guarantor Name"}));
        b.applicantName(get.apply(new String[]{"(GTI) Applicant Name"}));
        b.beneficiaryName(get.apply(new String[]{"(GTI) Beneficiary Name"}));
        b.applicantQuickCode(get.apply(new String[]{"(GTI) Applicant Quick Code"}));
        b.beneficiaryQuickCode(get.apply(new String[]{"(GTI) Beneficiary Quick Code"}));
        b.guarantorQuickCode(get.apply(new String[]{"(GTI) Guarantor Quick Code"}));
        b.category(get.apply(new String[]{"Category"}));
        b.nominalCurrency(get.apply(new String[]{"(GTI) Nominal Currency"}));
        b.actualAmount(getBig.apply(new String[]{"(GTI) Actual Amount"}));
        b.issueDate(getDate.apply(new String[]{"(GTI) Issue Date"}));
        b.expiryDate(getDate.apply(new String[]{"(GTI) Expiry Date"}));
        b.guaranteeStatus(get.apply(new String[]{"(GTI) Guarantee Status"}));
        b.amdSystemId(get.apply(new String[]{"(AMD) System ID"}));
        b.amendmentDate(getDate.apply(new String[]{"(AMD) Amendment Date"}));
        b.newExpiryDate(getDate.apply(new String[]{"(AMD) New Expiry Date"}));
        b.amendmentStatus(get.apply(new String[]{"(AMD) Amendment Status"}));
        b.guaranteeTypeDetails(get.apply(new String[]{"(GTI) Guarantee Type Details"}));
        b.internalReference(get.apply(new String[]{"(CF) Internal Reference"}));
        b.newNominalAmount(getBig.apply(new String[]{"(AMD) New Nominal Amount"}));
        b.nominalAmountChange(getBig.apply(new String[]{"(AMD) Nominal Amount Increase/Decrease by"}));
        b.automaticExtensionPeriod(get.apply(new String[]{"(GTI) Automatic Extension Period"}));
        b.bankReferenceNumber(get.apply(new String[]{"(GTI) Bank Reference Number"}));
        b.treasuryFlag(getBool.apply(new String[]{"TREASURY FLAG"}));
        b.instrumentType(get.apply(new String[]{"(GTI) Instrument Type"}));
        b.businessGroup(get.apply(new String[]{"Business Group"}));
        b.releaseDate(getDate.apply(new String[]{"(GTI) Release Date"}));
        b.otherComments(get.apply(new String[]{"Other Comments"}));
        b.ratingTrigger(get.apply(new String[]{"RATING TRIGGER"}));
        b.separateAuthority(getBool.apply(new String[]{"SEPARATE AUTHORITY"}));
        b.consolidated(getBool.apply(new String[]{"CONSOLIDATED"}));
        b.gtyDisclosure1(get.apply(new String[]{"GTY-Disclosure1"}));
        b.gtyDisclosure2Subcategory(get.apply(new String[]{"GTY-Disclosure2- Subcategory"}));
        b.lcDisclosure1(get.apply(new String[]{"LC-Disclosure1"}));
        b.lcDisclosure2Subcategory(get.apply(new String[]{"LC-Disclousure2-Subcategory"}));
        b.contractNumber(get.apply(new String[]{"(GTI) Contract Number"}));

        return b.build();
    }
}