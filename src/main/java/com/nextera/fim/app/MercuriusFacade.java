package com.nextera.fim.app;

import com.nextera.fim.app.model.BasePublishableEvent;

public interface MercuriusFacade {
    void publishEvent(BasePublishableEvent event, Cause cause);
}