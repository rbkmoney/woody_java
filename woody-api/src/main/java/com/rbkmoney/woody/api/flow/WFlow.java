package com.rbkmoney.woody.api.flow;

import com.rbkmoney.woody.api.flow.concurrent.WCallable;
import com.rbkmoney.woody.api.flow.concurrent.WRunnable;
import com.rbkmoney.woody.api.generator.ConfiguredSnowflakeIdGenerator;
import com.rbkmoney.woody.api.generator.IdGenerator;
import com.rbkmoney.woody.api.trace.TraceData;
import com.rbkmoney.woody.api.trace.context.TraceContext;

import java.util.concurrent.Callable;

public class WFlow {

    private final IdGenerator traceIdGenerator;
    private final IdGenerator spanIdGenerator;

    public static IdGenerator createDefaultIdGenerator() {
        return new ConfiguredSnowflakeIdGenerator();
    }

    public WFlow() {
        this(createDefaultIdGenerator());
    }

    public WFlow(IdGenerator idGenerator) {
        this(idGenerator, idGenerator);
    }

    public WFlow(IdGenerator traceIdGenerator, IdGenerator spanIdGenerator) {
        this.traceIdGenerator = traceIdGenerator;
        this.spanIdGenerator = spanIdGenerator;
    }

    public static WRunnable create(Runnable runnable) {
        return new WRunnable(runnable, TraceContext.getCurrentTraceData());
    }

    public static WRunnable create(Runnable runnable, TraceData traceData) {
        return new WRunnable(runnable, traceData);
    }

    public static <T> WCallable<T> create(Callable<T> callable) {
        return new WCallable<>(callable, TraceContext.getCurrentTraceData());
    }

    public static <T> WCallable<T> create(Callable<T> callable, TraceData traceData) {
        return new WCallable<>(callable, traceData);
    }

    public static WRunnable createFork(Runnable runnable) {
        return create(runnable, new TraceData());
    }

    public static WRunnable createServiceFork(Runnable runnable, IdGenerator idGenerator) {
        return create(runnable, TraceContext.initNewServiceTrace(new TraceData(), idGenerator, idGenerator));
    }

    public static WRunnable createServiceFork(Runnable runnable, IdGenerator traceIdGenerator, IdGenerator spanIdGenerator) {
        return create(runnable, TraceContext.initNewServiceTrace(new TraceData(), traceIdGenerator, spanIdGenerator));
    }

    public static <T> WCallable<T> createFork(Callable<T> callable) {
        return create(callable, new TraceData());
    }

    public static <T> WCallable<T> createServiceFork(Callable<T> callable, IdGenerator idGenerator) {
        return create(callable, TraceContext.initNewServiceTrace(new TraceData(), idGenerator, idGenerator));
    }

    public static <T> WCallable<T> createServiceFork(Callable<T> callable, IdGenerator traceIdGenerator, IdGenerator spanIdGenerator) {
        return create(callable, TraceContext.initNewServiceTrace(new TraceData(), traceIdGenerator, spanIdGenerator));
    }

    public WRunnable createServiceFork(Runnable runnable) {
        return createServiceFork(runnable, traceIdGenerator, spanIdGenerator);
    }

    public <T> WCallable<T> createServiceFork(Callable<T> callable) {
        return createServiceFork(callable, traceIdGenerator, spanIdGenerator);
    }

}
