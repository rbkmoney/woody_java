package com.rbkmoney.woody.api.trace.context.metadata;

import com.rbkmoney.woody.api.trace.Metadata;

/**
 * Created by vpankrashkin on 20.01.17.
 */
public interface MetadataExtension<T> {
    T getValue(Metadata metadata);
    default T getValue(String key, Metadata metadata) {
        return getValue(metadata);
    }
    void setValue(T val, Metadata metadata);
    default void setValue(String key, T val, Metadata metadata) {
        setValue(val, metadata);
    }
}
