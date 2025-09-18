package com.nextera.fim.app.processor;

import com.nextera.fim.app.model.CollateralChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.math.BigDecimal;
import java.time.Instant;
import com.nextera.fim.app.model.StreamId; // Assuming it's used elsewhere correctly

public class ChangeCollateralCommandProcessor {
    private static final Logger logger = LoggerFactory.getLogger(ChangeCollateralCommandProcessor.class);

    public void processChange(String gtiSystemId, Instant changedTime, String guarantorName, BigDecimal actualAmount) {
        // Correctly construct the CollateralChangedEvent
        CollateralChangedEvent collateralChangedEvent = new CollateralChangedEvent(
            gtiSystemId, // It should be a String key, not a StreamId
            changedTime,
            guarantorName,
            actualAmount
        );

        // Further handling logic
        logger.info("Processing collateral change event: {}", collateralChangedEvent);
    }
}