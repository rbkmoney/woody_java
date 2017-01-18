package com.rbkmoney.woody.api.interceptor;

import com.rbkmoney.woody.api.flow.error.ErrorMapProcessor;
import com.rbkmoney.woody.api.flow.error.WErrorDefinition;
import com.rbkmoney.woody.api.trace.ContextSpan;
import com.rbkmoney.woody.api.trace.MetadataProperties;
import com.rbkmoney.woody.api.trace.TraceData;
import com.rbkmoney.woody.api.trace.context.TraceContext;

import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * Created by vpankrashkin on 17.01.17.
 */
public class ErrorMappingInterceptor extends EmptyCommonInterceptor {
    private final ErrorMapProcessor errorProcessor;
    private final BiConsumer<WErrorDefinition, ContextSpan> errDefConsumer;

    public ErrorMappingInterceptor(ErrorMapProcessor errorProcessor, BiConsumer<WErrorDefinition, ContextSpan> errDefConsumer) {
        Objects.requireNonNull(errorProcessor);
        Objects.requireNonNull(errDefConsumer);
        this.errorProcessor = errorProcessor;
        this.errDefConsumer = errDefConsumer;
    }

    @Override
    public boolean interceptResponse(TraceData traceData, Object providerContext, Object... contextParams) {
        WErrorDefinition errorDefinition = errorProcessor.processMapToDef(TraceContext.getCurrentTraceData());
        if (errorDefinition != null) {
            ContextSpan contextSpan = TraceContext.getCurrentTraceData().getActiveSpan();
            contextSpan.getMetadata().putValue(MetadataProperties.ERROR_DEFINITION, errorDefinition);
            errDefConsumer.accept(errorDefinition, contextSpan);
        }
        return true;
    }
}
