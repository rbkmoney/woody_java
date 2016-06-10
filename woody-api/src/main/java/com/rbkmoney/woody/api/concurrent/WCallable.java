package com.rbkmoney.woody.api.concurrent;

import com.rbkmoney.woody.api.MDCUtils;
import com.rbkmoney.woody.api.trace.TraceData;
import com.rbkmoney.woody.api.trace.context.TraceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;


public class WCallable<T> implements Callable<T> {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final TraceData traceData;
    private final Callable<T> wrappedCallable;

    public static <T> WCallable<T> create(Callable<T> callable) {
        return new WCallable<>(callable);
    }

    public static <T> WCallable<T> create(Callable<T> callable, TraceData traceData) {
        return new WCallable<>(callable, traceData);
    }

    public static <T> WCallable<T> createFork(Callable<T> callable) {
        return create(callable, new TraceData());
    }

    public Callable<T> getWrappedCallable() {
        return wrappedCallable;
    }

    public TraceData getTraceData() {
        return traceData;
    }


    private WCallable(Callable<T> callable) {
        this(callable, TraceContext.getCurrentTraceData());
    }

    private WCallable(Callable<T> wrappedCallable, TraceData traceData) {
        this.traceData = traceData;
        this.wrappedCallable = wrappedCallable;
    }

    @Override
    public T call() throws Exception {
        TraceData originalTraceData = TraceContext.getCurrentTraceData();
        TraceContext.setCurrentTraceData(getTraceData().cloneObject());

        if (traceData != originalTraceData) {
            MDCUtils.putContextIds(traceData.getActiveSpan().getSpan());
        }

        try {
            return getWrappedCallable().call();
        } finally {
            TraceContext.setCurrentTraceData(originalTraceData);
            MDCUtils.putContextIds(originalTraceData.getActiveSpan().getSpan());
        }
    }

}
