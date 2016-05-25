package com.rbkmoney.woody.thrift.impl.http.generator;

import com.rbkmoney.woody.api.generator.IdGenerator;

/**
 * Created by vpankrashkin on 06.05.16.
 */
public class TimestampIdGenerator implements IdGenerator {
    @Override
    public String generateId(long timestamp) {
        return Long.toString(timestamp);
    }

    @Override
    public String generateId(long timestamp, int counter) {
        return new StringBuilder().append(timestamp).append(':').append(counter).toString();
    }
}
