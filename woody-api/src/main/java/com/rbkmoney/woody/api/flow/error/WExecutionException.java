package com.rbkmoney.woody.api.flow.error;

import com.rbkmoney.woody.api.trace.TraceData;

public class WExecutionException extends Exception {

    private TraceData traceData;

    public WExecutionException(Throwable cause, TraceData traceData) {
        super(cause);
        this.traceData = new TraceData(traceData, true);
    }

    public TraceData getTraceData() {
        return traceData;
    }
}
