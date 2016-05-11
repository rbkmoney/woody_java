package com.rbkmoney.woody.api.trace;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by vpankrashkin on 21.04.16.
 */
public class Metadata {
    private static final int DEFAULT_INIT_SIZE = 8;
    private static final float DEFAULT_LOAD_FACTOR = 0.75f;

    private Map<String, Object> values = createStore(DEFAULT_INIT_SIZE, DEFAULT_LOAD_FACTOR);

    public <T> T getValue(String key) {
        return (T) values.get(key);
    }

    public <T> T removeValue(String key) {
        return (T) values.remove(key);
    }

    public <T> T putValue(String key, Object value) {
        return (T) values.put(key, value);
    }

    public boolean containsKey(String key) {
        return values.containsKey(key);
    }

    public Collection<String> getKeys() {
        return values.keySet();
    }


    public void reset() {
        values = createStore(DEFAULT_INIT_SIZE, DEFAULT_LOAD_FACTOR);
    }

    private static Map<String, Object> createStore(int size, float loadFactor) {
        return new HashMap<>(size, loadFactor);
    }

}
