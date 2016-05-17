package com.rbkmoney.woody.api.trace.context;

import com.rbkmoney.woody.api.proxy.InstanceMethodCaller;
import com.rbkmoney.woody.api.proxy.MethodCallTracer;

/**
 * Created by vpankrashkin on 04.05.16.
 */
public class EmptyTracer implements MethodCallTracer {
    @Override
    public void beforeCall(Object[] args, InstanceMethodCaller caller) {

    }

    @Override
    public void afterCall(Object[] args, InstanceMethodCaller caller, Object result) {

    }

    @Override
    public void callError(Object[] args, InstanceMethodCaller caller, Throwable error) {

    }
}
