package com.rbkmoney.woody.api.trace.context;


import com.rbkmoney.woody.api.proxy.InstanceMethodCaller;
import com.rbkmoney.woody.api.proxy.MethodCallTracer;

/**
 * Created by vpankrashkin on 25.04.16.
 */
public class EventListenerTracer implements MethodCallTracer {

    private final MethodCallTracer callTracer;
    private final Runnable beforeCallListener;
    private final Runnable afterCallListener;
    private final Runnable errListener;

    public EventListenerTracer(MethodCallTracer callTracer) {
        this(callTracer, null, null, null);
    }

    public EventListenerTracer(MethodCallTracer callTracer, Runnable beforeCallListener, Runnable afterCallListener, Runnable errListener) {
        if (callTracer == null) {
            throw new NullPointerException("Tracer or listener cannot be null");
        }
        this.callTracer = callTracer;
        this.beforeCallListener = beforeCallListener != null ? beforeCallListener : () -> {
        };
        this.afterCallListener = afterCallListener != null ? afterCallListener : () -> {
        };
        this.errListener = errListener != null ? errListener : () -> {
        };
    }

    @Override
    public void beforeCall(Object[] args, InstanceMethodCaller caller) {
        callTracer.beforeCall(args, caller);
        beforeCallListener.run();
    }

    @Override
    public void afterCall(Object[] args, InstanceMethodCaller caller, Object result) {
        callTracer.afterCall(args, caller, result);
        afterCallListener.run();
    }

    @Override
    public void callError(Object[] args, InstanceMethodCaller caller, Throwable error) {
        callTracer.callError(args, caller, error);
        errListener.run();
    }
}
