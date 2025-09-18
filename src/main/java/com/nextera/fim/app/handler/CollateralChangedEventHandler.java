package com.nextera.fim.app.handler;

import com.nextera.fim.app.model.CollateralChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for processing CollateralChangedEvents.
 * Performs operations like logging or business logic execution upon event receipt.
 */
public class CollateralChangedEventHandler {

    private static final Logger logger = LoggerFactory.getLogger(CollateralChangedEventHandler.class);

    /**
     * Handles a CollateralChangedEvent.
     *
     * @param event The event to process
     */
    public void handleEvent(CollateralChangedEvent event) {
        // Log event details
        logger.info("Handling CollateralChangedEvent: Collateral Key - {}, Guarantor Name - {}, Actual Amount - {}",
                event.getCollateralKey(), event.getGuarantorName(), event.getActualAmount());
        
        // Example logic: Process the event
        // (e.g., update a database, notify other systems, etc.)
        processCollateralChange(event);
    }

    private void processCollateralChange(CollateralChangedEvent event) {
        // Placeholder for business logic
        // For example, update records in a database
        logger.debug("Processing collateral change with key: {}", event.getCollateralKey());
        
        // Assume this method communicates with a database or performs calculations
        // Actual implementation would depend on your business requirements
    }
}