package com.rbkmoney.woody.api.trace.context;

import com.rbkmoney.woody.api.generator.IdGenerator;
import com.rbkmoney.woody.api.trace.Span;
import com.rbkmoney.woody.api.trace.TraceData;

import static com.rbkmoney.woody.api.generator.IdGenerator.NO_PARENT_ID;

/**
 * Created by vpankrashkin on 25.04.16.
 */
public class TraceContext {
    private final static ThreadLocal<TraceData> currentTraceData = ThreadLocal.withInitial(() -> new TraceData());

    public static TraceData getCurrentTraceData() {
        return currentTraceData.get();
    }

    public static void setCurrentTraceData(TraceData traceData) {
        if (traceData == null) {
            currentTraceData.remove();
        } else {
            currentTraceData.set(traceData);
        }
    }

    public static void reset() {
        //accept the idea that limited set of objects (cleaned TraceData) will stay bound to thread after instance death
        //currently will lead to memory leak if lots of TraceContext classloads (which means lots of static thread locals) occurs in same thread
        TraceData traceData = getCurrentTraceData();
        if (traceData != null) {
            traceData.reset();
        }

    }

    public static TraceContext forClient(IdGenerator idGenerator, Runnable postInit, Runnable preDestroy) {
        return new TraceContext(idGenerator, postInit, preDestroy);
    }

    public static TraceContext forServer(IdGenerator idGenerator, Runnable postInit, Runnable preDestroy) {
        return new TraceContext(idGenerator, postInit, preDestroy);
    }

    private final IdGenerator idGenerator;
    private final Runnable postInit;
    private final Runnable preDestroy;
    private final boolean isAuto;
    private final boolean isClient;

    public TraceContext(IdGenerator idGenerator) {
        this(idGenerator, () -> {
        }, () -> {
        });
    }

    public TraceContext(IdGenerator idGenerator, Runnable postInit, Runnable preDestroy) {
        this.idGenerator = idGenerator;
        this.postInit = postInit;
        this.preDestroy = preDestroy;
        this.isAuto = true;
        this.isClient = false;
    }

    public TraceContext(IdGenerator idGenerator, Runnable postInit, Runnable preDestroy, boolean isClient) {
        this.idGenerator = idGenerator;
        this.postInit = postInit;
        this.preDestroy = preDestroy;
        this.isAuto = false;
        this.isClient = isClient;
    }

    /**
     * Server span must be already read and set, mustn't be invoked if any transport problems occurred
     */
    public void init() {
        TraceData traceData = getCurrentTraceData();
        if (isClientInit(traceData)) {
            initClientContext(traceData);
        } else {
            initServerContext(traceData);
        }
        postInit.run();
    }

    public void destroy() {
        TraceData traceData = getCurrentTraceData();
        boolean isClient = isClientDestroy(traceData);
        setDuration(isClient ? traceData.getClientSpan().getSpan() : traceData.getServerSpan().getSpan());
        try {
            preDestroy.run();
        } finally {
            if (isClient) {
                destroyClientContext(traceData);
            } else {
                destroyServerContext(traceData);
            }
        }
    }

    private void initClientContext(TraceData traceData) {
        long timestamp = System.currentTimeMillis();
        Span clientSpan = traceData.getClientSpan().getSpan();
        Span serverSpan = traceData.getServerSpan().getSpan();

        boolean root = traceData.isRoot();
        String traceId = root ? idGenerator.generateId(timestamp) : serverSpan.getTraceId();
        if (root) {
            clientSpan.setId(traceId);
            clientSpan.setParentId(NO_PARENT_ID);
        } else {
            clientSpan.setId(idGenerator.generateId(timestamp, traceData.getServerSpan().getCounter().incrementAndGet()));
            clientSpan.setParentId(serverSpan.getId());
        }
        clientSpan.setTraceId(traceId);
        clientSpan.setTimestamp(timestamp);
    }

    private void destroyClientContext(TraceData traceData) {
        traceData.getClientSpan().reset();
    }

    private void initServerContext(TraceData traceData) {
        long timestamp = System.currentTimeMillis();
        traceData.getServerSpan().getSpan().setTimestamp(timestamp);
    }

    private void destroyServerContext(TraceData traceData) {
        TraceContext.reset();
    }

    private void setDuration(Span span) {
        span.setDuration(System.currentTimeMillis() - span.getTimestamp());
    }

    private boolean isClientInit(TraceData traceData) {
        return isAuto ? isClientInitAuto(traceData) : isClient;
    }

    private boolean isClientDestroy(TraceData traceData) {
        return isAuto ? isClientDestroyAuto(traceData) : isClient;
    }

    private boolean isClientInitAuto(TraceData traceData) {
        Span serverSpan = traceData.getServerSpan().getSpan();

        assert !(traceData.getClientSpan().isStarted() & traceData.getServerSpan().isStarted());
        assert !(traceData.getClientSpan().isFilled() & traceData.getServerSpan().isFilled());

        return serverSpan.isFilled() ? serverSpan.isStarted() : true;

    }

    private boolean isClientDestroyAuto(TraceData traceData) {
        assert !(traceData.getClientSpan().isStarted() || traceData.getServerSpan().isStarted());

        return traceData.getServerSpan().isStarted() ? traceData.getClientSpan().isStarted() : true;

    }

}
