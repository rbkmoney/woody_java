package com.rbkmoney.woody.api.trace.context;

import com.rbkmoney.woody.api.MDCUtils;
import com.rbkmoney.woody.api.generator.IdGenerator;
import com.rbkmoney.woody.api.trace.Span;
import com.rbkmoney.woody.api.trace.TraceData;

import java.util.Optional;

/**
 * Created by vpankrashkin on 25.04.16.
 */
public class TraceContext {
    public static final String NO_PARENT_ID = "undefined";

    private final static ThreadLocal<TraceData> currentTraceData = ThreadLocal.withInitial(() -> new TraceData());

    public static TraceData getCurrentTraceData() {
        return currentTraceData.get();
    }

    public static TraceData initNewServiceTrace(TraceData traceData, IdGenerator traceIdGenerator, IdGenerator spanIdGenerator) {
        return initServiceTraceData(traceData, traceIdGenerator, spanIdGenerator);
    }

    public static TraceData initServiceTraceData(TraceData traceData, IdGenerator traceIdGenerator, IdGenerator spanIdGenerator) {
        if ((traceData.isRoot())) {
            initSpan(traceIdGenerator, spanIdGenerator, traceData, false);
        }
        return traceData;
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

    public static TraceContext forClient(IdGenerator idGenerator, Runnable postInit, Runnable preDestroy, Runnable preErrDestroy) {
        return new TraceContext(idGenerator, postInit, preDestroy, preErrDestroy);
    }

    public static TraceContext forService(Runnable postInit, Runnable preDestroy, Runnable preErrDestroy) {
        return new TraceContext(null, postInit, preDestroy, preErrDestroy);
    }

    private final IdGenerator traceIdGenerator;
    private final IdGenerator spanIdGenerator;
    private final Runnable postInit;
    private final Runnable preDestroy;
    private final Runnable preErrDestroy;
    private final boolean isAuto;
    private final boolean isClient;

    public TraceContext(IdGenerator idGenerator) {
        this(idGenerator, idGenerator);
    }

    public TraceContext(IdGenerator traceIdGenerator, IdGenerator spanIdGenerator) {
        this(traceIdGenerator, spanIdGenerator, () -> {
        }, () -> {
        }, () -> {
        });
    }

    public TraceContext(IdGenerator idGenerator, Runnable postInit, Runnable preDestroy, Runnable preErrDestroy) {
        this(idGenerator, idGenerator, postInit, preDestroy, preErrDestroy);
    }

    public TraceContext(IdGenerator traceIdGenerator, IdGenerator spanIdGenerator, Runnable postInit, Runnable preDestroy, Runnable preErrDestroy) {
        this(traceIdGenerator, spanIdGenerator, postInit, preDestroy, preErrDestroy, Optional.empty());
    }

    public TraceContext(IdGenerator idGenerator, Runnable postInit, Runnable preDestroy, Runnable preErrDestroy, boolean isClient) {
        this(idGenerator, idGenerator, postInit, preDestroy, preErrDestroy, Optional.of(isClient));
    }

    private TraceContext(IdGenerator traceIdGenerator, IdGenerator spanIdGenerator, Runnable postInit, Runnable preDestroy, Runnable preErrDestroy, Optional<Boolean> isClient) {
        this.traceIdGenerator = traceIdGenerator;
        this.spanIdGenerator = spanIdGenerator;
        this.postInit = postInit;
        this.preDestroy = preDestroy;
        this.preErrDestroy = preErrDestroy;
        if (isClient.isPresent()) {
            this.isAuto = false;
            this.isClient = isClient.get();
        } else {
            this.isAuto = true;
            this.isClient = false;
        }
    }

    /**
     * Server span must be already read and set, mustn't be invoked if any transport problems occurred
     */
    public void init() {
        TraceData traceData = getCurrentTraceData();
        if (isClientInit(traceData)) {
            initClientContext(traceData);
        } else {
            initServiceContext(traceData);
        }

        MDCUtils.putContextIds(traceData.getActiveSpan().getSpan());

        postInit.run();
    }

    public void destroy() {
        destroy(false);
    }

    public void destroy(boolean onError) {
        TraceData traceData = getCurrentTraceData();
        boolean isClient = isClientDestroy(traceData);
        setDuration(traceData, isClient);
        try {
            if (onError) {
                preErrDestroy.run();
            } else {
                preDestroy.run();
            }
        } finally {
            if (isClient) {
                destroyClientContext(traceData);
            } else {
                destroyServiceContext(traceData);
            }

            if (traceData.getServiceSpan().isFilled()) {
                MDCUtils.putContextIds(traceData.getServiceSpan().getSpan());
            } else {
                MDCUtils.removeContextIds();
            }

        }
    }

    public void setDuration() {
        setDuration(getCurrentTraceData(), isClient);
    }

    private void initClientContext(TraceData traceData) {
        initSpan(traceIdGenerator, spanIdGenerator, traceData, true);
    }

    private static TraceData initSpan(IdGenerator traceIdGenerator, IdGenerator spanIdGenerator, TraceData traceData, boolean isClient) {

        long timestamp = System.currentTimeMillis();
        Span clientSpan = traceData.getClientSpan().getSpan();
        Span serviceSpan = traceData.getServiceSpan().getSpan();

        Span initSpan = isClient ? clientSpan : serviceSpan;

        boolean root = traceData.isRoot();
        String traceId = root ? traceIdGenerator.generateId() : serviceSpan.getTraceId();
        if (root) {
            initSpan.setId(spanIdGenerator.generateId());
            initSpan.setParentId(NO_PARENT_ID);
        } else {
            initSpan.setId(spanIdGenerator.generateId("", traceData.getServiceSpan().getCounter().incrementAndGet()));
            initSpan.setParentId(serviceSpan.getId());
        }
        initSpan.setTraceId(traceId);
        initTime(traceData, timestamp, isClient);
        return traceData;
    }

    private static void initTime(TraceData traceData, long timestamp, boolean isClient) {
        (isClient ? traceData.getClientSpan() : traceData.getServiceSpan()).getSpan().setTimestamp(timestamp);
    }

    private void destroyClientContext(TraceData traceData) {
        traceData.getClientSpan().reset();
    }

    private void initServiceContext(TraceData traceData) {
        initTime(traceData, System.currentTimeMillis(), false);
    }

    private void destroyServiceContext(TraceData traceData) {
        TraceContext.reset();
    }

    private void setDuration(TraceData traceData, boolean isClient) {
        Span span = (isClient ? traceData.getClientSpan().getSpan() : traceData.getServiceSpan().getSpan());
        span.setDuration(System.currentTimeMillis() - span.getTimestamp());
    }

    private boolean isClientInit(TraceData traceData) {
        return isAuto ? isClientInitAuto(traceData) : isClient;
    }

    private boolean isClientDestroy(TraceData traceData) {
        return isAuto ? isClientDestroyAuto(traceData) : isClient;
    }

    private boolean isClientInitAuto(TraceData traceData) {
        Span serverSpan = traceData.getServiceSpan().getSpan();

        assert !(traceData.getClientSpan().isStarted() & traceData.getServiceSpan().isStarted());
        assert !(traceData.getClientSpan().isFilled() & traceData.getServiceSpan().isFilled());

        return serverSpan.isFilled() ? serverSpan.isStarted() : true;
    }

    private boolean isClientDestroyAuto(TraceData traceData) {
        assert (traceData.getClientSpan().isStarted() || traceData.getServiceSpan().isStarted());

        return traceData.getServiceSpan().isStarted() ? traceData.getClientSpan().isStarted() : true;
    }

}
