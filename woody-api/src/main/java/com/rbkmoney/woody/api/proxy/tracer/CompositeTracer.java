package com.rbkmoney.woody.api.proxy.tracer;

import com.rbkmoney.woody.api.proxy.InstanceMethodCaller;

import java.util.Arrays;
import java.util.Collection;

public class CompositeTracer implements MethodCallTracer {
    private final MethodCallTracer[] tracers;

    public CompositeTracer(MethodCallTracer... callTracers) {
        this(Arrays.asList(callTracers));
    }

    public CompositeTracer(Collection<? extends MethodCallTracer> tracers) {
        this.tracers = tracers.toArray(new MethodCallTracer[0]);
    }

    @Override
    public void beforeCall(Object[] args, InstanceMethodCaller caller) throws Exception {
        for (int i = 0; i < tracers.length; ++i) {
            tracers[i].beforeCall(args, caller);
        }
    }

    @Override
    public void afterCall(Object[] args, InstanceMethodCaller caller, Object result) throws Exception {
        for (int i = 0; i < tracers.length; ++i) {
            tracers[i].afterCall(args, caller, result);
        }
    }

    @Override
    public void callError(Object[] args, InstanceMethodCaller caller, Throwable error) throws Exception {
        for (int i = 0; i < tracers.length; ++i) {
            tracers[i].callError(args, caller, error);
        }
    }
}
