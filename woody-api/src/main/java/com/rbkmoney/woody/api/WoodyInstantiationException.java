package com.rbkmoney.woody.api;

/**
 * Created by vpankrashkin on 05.05.16.
 */
public class WoodyInstantiationException extends RuntimeException {
    public WoodyInstantiationException() {
        super();
    }

    public WoodyInstantiationException(String message) {
        super(message);
    }

    public WoodyInstantiationException(String message, Throwable cause) {
        super(message, cause);
    }

    public WoodyInstantiationException(Throwable cause) {
        super(cause);
    }

    protected WoodyInstantiationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
