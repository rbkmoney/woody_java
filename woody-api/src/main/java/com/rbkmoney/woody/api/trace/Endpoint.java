package com.rbkmoney.woody.api.trace;

public interface Endpoint<T> {
    String getStringValue();

    T getValue();
}
