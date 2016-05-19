package com.rbkmoney.woody.thrift.impl.http.interceptor;

import com.rbkmoney.woody.thrift.impl.http.transport.TTransportErrorType;

/**
 * Created by vpankrashkin on 11.05.16.
 */
public class THRequestInterceptionException extends RuntimeException {
    private final TTransportErrorType errorType;
    private final Object reason;

    public THRequestInterceptionException(TTransportErrorType transportErrorType, Object reason) {
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
