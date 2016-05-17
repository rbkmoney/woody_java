package com.rbkmoney.woody.api.trace.context;


import com.rbkmoney.woody.api.proxy.InstanceMethodCaller;
import com.rbkmoney.woody.api.proxy.MethodCallTracer;

/**
 * Created by vpankrashkin on 25.04.16.
 */
public class EventTracer implements MethodCallTracer {

    private final Runnable beforeCallListener;
    private final Runnable afterCallListener;
    private final Runnable errListener;

    public EventTracer() {
        this(null, null, null);
    }

    public EventTracer(Runnable beforeCallListener, Runnable afterCallListener, Runnable errListener) {
        this.beforeCallListener = beforeCallListener != null ? beforeCallListener : () -> {
        };
        this.afterCallListener = afterCallListener != null ? afterCallListener : () -> {
        };
        this.errListener = errListener != null ? errListener : () -> {
        };
    }

    @Override
    public void beforeCall(Object[] args, InstanceMethodCaller caller) {
        beforeCallListener.run();
    }

    @Override
    public void afterCall(Object[] args, InstanceMethodCaller caller, Object result) {
        afterCallListener.run();
    }

    @Override
    public void callError(Object[] args, InstanceMethodCaller caller, Throwable error) {
        errListener.run();
    }
}
