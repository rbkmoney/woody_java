package com.rbkmoney.woody.thrift.impl.http;

/**
 * Created by vpankrashkin on 22.12.16.
 */
public class THResponseInfo {
    private final int status;
    private final String errClass;
    private final String errReason;

    public THResponseInfo(int status, String errClass, String errReason) {
        this.status = status;
        this.errClass = errClass;
        this.errReason = errReason;
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

    @Override
    public String toString() {
        return "THResponseInfo{" +
                "status=" + status +
                ", errClass='" + errClass + '\'' +
                ", errReason='" + errReason + '\'' +
                '}';
    }
}
