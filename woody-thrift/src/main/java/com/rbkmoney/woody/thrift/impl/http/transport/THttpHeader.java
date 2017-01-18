package com.rbkmoney.woody.thrift.impl.http.transport;

/**
 * Created by vpankrashkin on 11.05.16.
 */
public enum THttpHeader {
    TRACE_ID("x-rbk-trace-id"),
    SPAN_ID("x-rbk-span-id"),
    PARENT_ID("x-rbk-parent-id"),
    ERROR_CLASS("x-rbk-error-class"),
    ERROR_REASON("x-rbk-error-reason");

    private String key;

    THttpHeader(String name) {
        this.key = name;
    }

    public String getKey() {
        return key;
    }
}
