package com.rbkmoney.woody.api.proxy;

/**
 * Created by vpankrashkin on 23.04.16.
 */
public interface MethodCallTracer {
    void beforeCall(Object[] args, InstanceMethodCaller caller);

    void afterCall(Object[] args, InstanceMethodCaller caller, Object result);

    void callError(Object[] args, InstanceMethodCaller caller, Throwable error);
}
