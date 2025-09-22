package com.nextera.fim.app;

public interface ContinuumEventHandler<T> {
    SideEffects handle(T event);
}