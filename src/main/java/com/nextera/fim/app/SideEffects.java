package com.nextera.fim.app;

import java.util.ArrayList;
import java.util.List;

public class SideEffects {

    private final List<String> effects = new ArrayList<>();

    public void addEffect(String effect) {
        effects.add(effect);
    }

    public List<String> getEffects() {
        return new ArrayList<>(effects);
    }

    @Override
    public String toString() {
        return "SideEffects{" +
                "effects=" + effects +
                '}';
    }
}