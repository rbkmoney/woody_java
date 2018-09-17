package com.rbkmoney.woody.thrift.impl.http.transport;

/**
 * Created by vpankrashkin on 11.05.16.
 */
public enum THttpHeader {
    TRACE_ID("woody.trace-id", "x-rbk-trace-id"),
    SPAN_ID("woody.span-id", "x-rbk-span-id"),
    PARENT_ID("woody.parent-id", "x-rbk-parent-id"),
    DEADLINE("woody.deadline", "x-rbk-deadline"),
    ERROR_CLASS("woody.error-class", "x-rbk-error-class"),
    ERROR_REASON("woody.error-reason", "x-rbk-error-reason"),
    META("woody.meta-", "x-rbk-meta-");

    private String key;

    @Deprecated
    private String oldKey;

    THttpHeader(String key, String oldKey) {
        this.key = key;
        this.oldKey = oldKey;
    }

    public String getKey() {
        return key;
    }

    @Deprecated
    public String getOldKey() {
        return oldKey;
    }
}
