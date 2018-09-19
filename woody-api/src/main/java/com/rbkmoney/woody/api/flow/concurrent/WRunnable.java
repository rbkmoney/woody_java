package com.rbkmoney.woody.api.flow.concurrent;

import com.rbkmoney.woody.api.MDCUtils;
import com.rbkmoney.woody.api.trace.TraceData;
import com.rbkmoney.woody.api.trace.context.TraceContext;

public class WRunnable implements Runnable {

    private final TraceData traceData;
    private final Runnable wrappedRunnable;

    public WRunnable(Runnable runnable, TraceData traceData) {
        if (runnable == null || traceData == null) {
            throw new NullPointerException("Null arguments're not allowed");
        }
        this.wrappedRunnable = runnable;
        this.traceData = traceData;
    }

    public Runnable geWrappedRunnable() {
        return wrappedRunnable;
    }

    public TraceData getTraceData() {
        return traceData;
    }

    @Override
    public void run() {
        TraceData originalTraceData = TraceContext.getCurrentTraceData();
        TraceContext.setCurrentTraceData(getTraceData().cloneObject());

        if (traceData != originalTraceData) {
            MDCUtils.putSpanData(traceData.getActiveSpan().getSpan());
        }

        try {
            geWrappedRunnable().run();
        } finally {
            TraceContext.setCurrentTraceData(originalTraceData);
            MDCUtils.putSpanData(originalTraceData.getActiveSpan().getSpan());
        }
    }
}
