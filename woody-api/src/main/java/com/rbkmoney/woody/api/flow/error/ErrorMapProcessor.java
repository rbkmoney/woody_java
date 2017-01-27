package com.rbkmoney.woody.api.flow.error;

import com.rbkmoney.woody.api.trace.ContextSpan;
import com.rbkmoney.woody.api.trace.ContextUtils;
import com.rbkmoney.woody.api.trace.MetadataProperties;
import com.rbkmoney.woody.api.trace.TraceData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vpankrashkin on 20.12.16.
 */
public class ErrorMapProcessor {
    private final boolean isClient;
    private final List<WErrorMapper> mappers;

    public ErrorMapProcessor(boolean isClient, List<WErrorMapper> mappers) {
        this.isClient = isClient;
        this.mappers = new ArrayList<>(mappers);
    }

    public WErrorDefinition processMapToDef(TraceData traceData) {
        ContextSpan contextSpan = isClient ? traceData.getClientSpan() : traceData.getServiceSpan();
        Throwable t = ContextUtils.getCallError(contextSpan);
        WErrorDefinition errorDefinition = null;
        if (t != null) {
            for (int i = 0; errorDefinition == null && i < mappers.size(); ++i) {
                errorDefinition = mappers.get(i).mapToDef(t, contextSpan);
            }
        }
        return errorDefinition;
    }

    /**
     * @throws RuntimeException expected if any error occurs
     * */
    public Exception processMapToError(TraceData traceData) {
        ContextSpan contextSpan = isClient ? traceData.getClientSpan() : traceData.getServiceSpan();
        WErrorDefinition errorDefinition = contextSpan.getMetadata().getValue(MetadataProperties.ERROR_DEFINITION);
        Exception ex = null;

        if (errorDefinition != null) {
            for (int i = 0; ex == null && i < mappers.size(); ++i) {
                ex = mappers.get(i).mapToError(errorDefinition, contextSpan);
            }
        }
        return ex;
    }
}
