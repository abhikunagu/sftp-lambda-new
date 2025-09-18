package com.nextera.fim.app.model;

public abstract class BasePublishableEvent {
    private final StreamId streamId;

    public BasePublishableEvent(StreamId streamId) {
        this.streamId = streamId;
    }

    public StreamId getStreamId() {
        return streamId;
    }
}