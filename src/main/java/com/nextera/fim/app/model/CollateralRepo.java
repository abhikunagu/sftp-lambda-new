package com.nextera.fim.app.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

// A simple in-memory repository for demonstration purposes
public class CollateralRepo {

    private final Map<String, Collateral> collateralStore = new HashMap<>();

    public Optional<Collateral> get(String collateralKey) {
        return Optional.ofNullable(collateralStore.get(collateralKey));
    }

    public void save(Collateral collateral) {
        collateralStore.put(collateral.getCollateralKey(), collateral);
    }
}