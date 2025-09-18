package com.nextera.fim.app.model;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Represents an event where collateral has changed.
 */
public class CollateralChangedEvent {
    private String collateralKey; // Unique identifier for the collateral
    private Instant changedTime; // Timestamp of when the change occurred
    private String guarantorName; // Name of the guarantor
    private BigDecimal actualAmount; // Amount involved in the change

    /**
     * Constructs a CollateralChangedEvent with specific details.
     *
     * @param collateralKey  Unique identifier for the collateral.
     * @param changedTime    Time the collateral has changed.
     * @param guarantorName  Name of the guarantor associated with the collateral.
     * @param actualAmount   Actual amount of the collateral involved in the change.
     */
    public CollateralChangedEvent(String collateralKey, Instant changedTime, String guarantorName, BigDecimal actualAmount) {
        this.collateralKey = collateralKey;
        this.changedTime = changedTime;
        this.guarantorName = guarantorName;
        this.actualAmount = actualAmount;
    }

    public String getCollateralKey() {
        return collateralKey;
    }

    public Instant getChangedTime() {
        return changedTime;
    }

    public String getGuarantorName() {
        return guarantorName;
    }

    public BigDecimal getActualAmount() {
        return actualAmount;
    }

    @Override
    public String toString() {
        return "CollateralChangedEvent{" +
               "collateralKey='" + collateralKey + '\'' +
               ", changedTime=" + changedTime +
               ", guarantorName='" + guarantorName + '\'' +
               ", actualAmount=" + actualAmount +
               '}';
    }
}