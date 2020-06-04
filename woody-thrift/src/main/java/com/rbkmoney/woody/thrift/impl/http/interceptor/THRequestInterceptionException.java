package com.rbkmoney.woody.thrift.impl.http.interceptor;

import com.rbkmoney.woody.thrift.impl.http.transport.TTransportErrorType;

public class THRequestInterceptionException extends RuntimeException {
    private final TTransportErrorType errorType;
    private final Object reason;

    public THRequestInterceptionException(TTransportErrorType transportErrorType, Object reason) {
        this(transportErrorType, reason, null);
    }

    public THRequestInterceptionException(TTransportErrorType transportErrorType, Object reason, Throwable cause) {
        super(cause);
        errorType = transportErrorType;
        this.reason = reason;
    }

    public TTransportErrorType getErrorType() {
        return errorType;
    }

    public Object getReason() {
        return reason;
    }
}
