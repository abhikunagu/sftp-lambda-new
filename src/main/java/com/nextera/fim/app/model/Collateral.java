package com.nextera.fim.app.model;

public class Collateral {

    private final String collateralKey;
    // Other fields can be added as needed

    public Collateral(String collateralKey) {
        this.collateralKey = collateralKey;
        // Initialize other fields as necessary
    }

    public String getCollateralKey() {
        return collateralKey;
    }

    // Additional getter/setter methods and logic
}