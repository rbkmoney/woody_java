package com.rbkmoney.woody.api.flow.concurrent;

import com.rbkmoney.woody.api.MDCUtils;
import com.rbkmoney.woody.api.trace.TraceData;
import com.rbkmoney.woody.api.trace.context.TraceContext;

import java.util.concurrent.Callable;

public class WCallable<T> implements Callable<T> {

    private final TraceData traceData;
    private final Callable<T> wrappedCallable;

    public Callable<T> getWrappedCallable() {
        return wrappedCallable;
    }

    public TraceData getTraceData() {
        return traceData;
    }

    public WCallable(Callable<T> wrappedCallable, TraceData traceData) {
        if (wrappedCallable == null || traceData == null) {
            throw new NullPointerException("Null arguments're not allowed");
        }
        this.traceData = traceData;
        this.wrappedCallable = wrappedCallable;
    }

    @Override
    public T call() throws Exception {
        TraceData originalTraceData = TraceContext.getCurrentTraceData();
        TraceContext.setCurrentTraceData(getTraceData().cloneObject());

        if (traceData != originalTraceData) {
            MDCUtils.putSpanData(traceData.getActiveSpan().getSpan());
        }

        try {
            return getWrappedCallable().call();
        } finally {
            TraceContext.setCurrentTraceData(originalTraceData);
            MDCUtils.putSpanData(originalTraceData.getActiveSpan().getSpan());
        }
    }

}
