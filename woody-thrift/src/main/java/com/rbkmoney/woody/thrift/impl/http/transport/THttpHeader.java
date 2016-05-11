package com.rbkmoney.woody.thrift.impl.http.transport;

/**
 * Created by vpankrashkin on 11.05.16.
 */
public enum THttpHeader {
    TRACE_ID("x-rbk-trace-id"),
    SPAN_ID("x-rbk-span-id"),
    PARENT_ID("x-rbk-parent-id"),
    ERROR_LOGIC("x-rbk-rpc-error-logic"),
    ERROR_THRIFT("x-rbk-rpc-error-thrift");

    private String value;

    THttpHeader(String name) {
        this.value = name;
    }

    public String getKeyValue() {
        return value;
    }
}
