package com.rbkmoney.woody.api.proxy.tracer;

import com.rbkmoney.woody.api.proxy.InstanceMethodCaller;

/**
 * Created by vpankrashkin on 23.04.16.
 */
public interface MethodCallTracer {
    void beforeCall(Object[] args, InstanceMethodCaller caller) throws Exception;

    void afterCall(Object[] args, InstanceMethodCaller caller, Object result) throws Exception;

    void callError(Object[] args, InstanceMethodCaller caller, Throwable error) throws Exception;

}
