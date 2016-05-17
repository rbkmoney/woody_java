package com.rbkmoney.woody.api.event;

/**
 * Created by vpankrashkin on 22.04.16.
 * <p>
 * Custom error call constants, specific for
 */
public enum ErrorType {
    /**
     * Any thrift errors (protocol, transport, etc).
     */
    PROVIDER_ERROR,

    /**
     * Error which is registered in service method declaration.
     */
    APPLICATION_KNOWN_ERROR,

    /**
     * Any other error which is not registered for calling method and doesn't refer to thrift errors.
     */
    APPLICATION_UNKNOWN_ERROR,

    /**
     * Any other error
     */
    OTHER

}
