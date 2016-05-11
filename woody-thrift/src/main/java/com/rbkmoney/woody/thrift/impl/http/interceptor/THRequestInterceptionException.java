package com.rbkmoney.woody.thrift.impl.http.interceptor;

import com.rbkmoney.woody.thrift.impl.http.transport.TTransportErrorType;

/**
 * Created by vpankrashkin on 11.05.16.
 */
public class THRequestInterceptionException extends RuntimeException {
    private TTransportErrorType errorType;

    public THRequestInterceptionException(TTransportErrorType transportErrorType) {
        super();
        errorType = transportErrorType;
    }

    public TTransportErrorType getErrorType() {
        return errorType;
    }
}
