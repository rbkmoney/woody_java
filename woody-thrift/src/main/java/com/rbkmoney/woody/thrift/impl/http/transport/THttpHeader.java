package com.rbkmoney.woody.thrift.impl.http.transport;

public enum THttpHeader {
    TRACE_ID("woody.trace-id"),
    SPAN_ID("woody.span-id"),
    PARENT_ID("woody.parent-id"),
    DEADLINE("woody.deadline"),
    ERROR_CLASS("woody.error-class"),
    ERROR_REASON("woody.error-reason"),
    META("woody.meta.");

    private String key;

    THttpHeader(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

}
