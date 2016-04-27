package com.rbkmoney.woody.api.generator;

/**
 * Created by vpankrashkin on 22.04.16.
 */
public interface IdGenerator {
    String NO_PARENT_ID = "undefined";

    String generateId(long timestamp);

    String generateId(long timestamp, int counter);
}
