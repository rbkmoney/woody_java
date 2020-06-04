package com.rbkmoney.woody.api.generator;

public class TimestampIdGenerator implements IdGenerator {

    @Override
    public String generateId() {
        return Long.toString(System.currentTimeMillis());
    }

    @Override
    public String generateId(String prefix) {
        return new StringBuilder().append(prefix).append(System.currentTimeMillis()).toString();
    }

    @Override
    public String generateId(String prefix, int counter) {
        return new StringBuilder().append(prefix).append(System.currentTimeMillis()).append(':').append(counter).toString();
    }
}
