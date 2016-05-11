package com.rbkmoney.woody.api.trace.context;

import com.rbkmoney.woody.api.proxy.InstanceMethodCaller;
import com.rbkmoney.woody.api.proxy.MethodCallTracer;

/**
 * Created by vpankrashkin on 26.04.16.
 *
 * Used to control context lifecycle on interface method call and return
 */
public class ContextTracer implements MethodCallTracer {
    private final TraceContext traceContext;
    private final MethodCallTracer targetTracer;

    public ContextTracer(TraceContext traceContext, MethodCallTracer targetTracer) {
        this.traceContext = traceContext;
        this.targetTracer = targetTracer;
    }

    @Override
    public void beforeCall(Object[] args, InstanceMethodCaller caller) {
        traceContext.init();
        targetTracer.beforeCall(args, caller);
    }

    @Override
    public void afterCall(Object[] args, InstanceMethodCaller caller, Object result) {
        try {
            targetTracer.afterCall(args, caller, result);
        } finally {
            traceContext.destroy();
        }
    }

    @Override
    public void callError(Object[] args, InstanceMethodCaller caller, Throwable error) {
        try {
            targetTracer.callError(args, caller, error);
        } finally {
            traceContext.destroy(true);
        }
    }
}
