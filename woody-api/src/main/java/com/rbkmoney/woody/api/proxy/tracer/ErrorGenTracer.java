package com.rbkmoney.woody.api.proxy.tracer;

import com.rbkmoney.woody.api.flow.error.ErrorMapProcessor;
import com.rbkmoney.woody.api.proxy.InstanceMethodCaller;
import com.rbkmoney.woody.api.trace.context.TraceContext;

/**
 * Created by vpankrashkin on 17.01.17.
 */
public class ErrorGenTracer extends EmptyTracer {
    private final ErrorMapProcessor errorProcessor;

    public ErrorGenTracer(ErrorMapProcessor errorProcessor) {
        this.errorProcessor = errorProcessor;
    }

    @Override
    public void afterCall(Object[] args, InstanceMethodCaller caller, Object result) throws Exception {
        process();
    }

    @Override
    public void callError(Object[] args, InstanceMethodCaller caller, Throwable error) throws Exception {
        process();
    }

    private void process() throws Exception {
        Exception ex = errorProcessor.processMapToError(TraceContext.getCurrentTraceData());
        if (ex != null) {
            throw ex;
        }
    }
}
