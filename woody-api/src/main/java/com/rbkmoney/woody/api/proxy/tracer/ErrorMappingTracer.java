package com.rbkmoney.woody.api.proxy.tracer;

import com.rbkmoney.woody.api.flow.error.ErrorMapProcessor;
import com.rbkmoney.woody.api.flow.error.WErrorDefinition;
import com.rbkmoney.woody.api.proxy.InstanceMethodCaller;
import com.rbkmoney.woody.api.trace.ContextSpan;
import com.rbkmoney.woody.api.trace.MetadataProperties;
import com.rbkmoney.woody.api.trace.context.TraceContext;

import java.util.Objects;
import java.util.function.BiConsumer;

public class ErrorMappingTracer extends EmptyTracer  {
    private final ErrorMapProcessor errorProcessor;
    private final BiConsumer<WErrorDefinition, ContextSpan> errDefConsumer;

    public ErrorMappingTracer(ErrorMapProcessor errorProcessor, BiConsumer<WErrorDefinition, ContextSpan> errDefConsumer) {
        Objects.requireNonNull(errorProcessor);
        Objects.requireNonNull(errDefConsumer);
        this.errorProcessor = errorProcessor;
        this.errDefConsumer = errDefConsumer;
    }


    @Override
    public void afterCall(Object[] args, InstanceMethodCaller caller, Object result) {
        processCall();
    }

    @Override
    public void callError(Object[] args, InstanceMethodCaller caller, Throwable error) {
        processCall();
    }

    private void processCall() {
        WErrorDefinition errorDefinition = errorProcessor.processMapToDef(TraceContext.getCurrentTraceData());
        if (errorDefinition != null) {
            ContextSpan contextSpan = TraceContext.getCurrentTraceData().getActiveSpan();
            contextSpan.getMetadata().putValue(MetadataProperties.ERROR_DEFINITION, errorDefinition);
            errDefConsumer.accept(errorDefinition, contextSpan);
        }
    }
}
