package com.rbkmoney.woody.thrift.impl.http;

public class THResponseInfo {
    private final int status;
    private final String errClass;
    private final String errReason;
    private final String message;

    public THResponseInfo(int status, String errClass, String errReason) {
        this(status, errClass, errReason, null);
    }

    public THResponseInfo(int status, String errClass, String errReason, String message) {
        this.status = status;
        this.errClass = errClass;
        this.errReason = errReason;
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public String getErrClass() {
        return errClass;
    }

    public String getErrReason() {
        return errReason;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "THResponseInfo{" +
                "status=" + status +
                ", errClass='" + errClass + '\'' +
                ", errReason='" + errReason + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
