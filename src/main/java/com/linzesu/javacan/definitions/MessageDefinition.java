package com.linzesu.javacan.definitions;

import java.util.HashMap;
import java.util.Map;

public class MessageDefinition {
    private long id;
    private final Map<String, SignalDefinition> signals = new HashMap<>();

    public MessageDefinition(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void addSignal(SignalDefinition signal) {
        signals.put(signal.getName(), signal);
    }

    public SignalDefinition getSignal(String name) {
        return signals.get(name);
    }

    public Iterable<SignalDefinition> getSignals() {
        return signals.values();
    }

}