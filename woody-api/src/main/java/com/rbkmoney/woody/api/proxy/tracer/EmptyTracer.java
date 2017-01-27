package com.rbkmoney.woody.api.proxy.tracer;

import com.rbkmoney.woody.api.proxy.InstanceMethodCaller;

/**
 * Created by vpankrashkin on 04.05.16.
 */
public class EmptyTracer implements MethodCallTracer {
    @Override
    public void beforeCall(Object[] args, InstanceMethodCaller caller) throws Exception {

    }

    @Override
    public void afterCall(Object[] args, InstanceMethodCaller caller, Object result) throws Exception {

    }

    @Override
    public void callError(Object[] args, InstanceMethodCaller caller, Throwable error) throws Exception {

    }
}
