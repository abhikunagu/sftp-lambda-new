package com.nextera.fim.app.model;

public class StreamId {
    private final String type;
    private final String id;

    private StreamId(String type, String id) {
        this.type = type;
        this.id = id;
    }

    public static StreamId of(String type, String id) {
        return new StreamId(type, id);
    }

    // Optional: Add getters for type and id if needed
}