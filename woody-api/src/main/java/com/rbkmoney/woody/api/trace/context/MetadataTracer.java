package com.rbkmoney.woody.api.trace.context;

import com.rbkmoney.woody.api.event.ClientEventType;
import com.rbkmoney.woody.api.event.ServiceEventType;
import com.rbkmoney.woody.api.proxy.InstanceMethodCaller;
import com.rbkmoney.woody.api.proxy.MethodCallTracer;
import com.rbkmoney.woody.api.trace.ContextSpan;
import com.rbkmoney.woody.api.trace.ContextUtils;
import com.rbkmoney.woody.api.trace.Metadata;
import com.rbkmoney.woody.api.trace.MetadataProperties;

/**
 * Created by vpankrashkin on 25.04.16.
 */
public class MetadataTracer implements MethodCallTracer {
    private final boolean isClient;
    private final boolean isAuto;

    public static MetadataTracer forClient() {
        return new MetadataTracer(true);
    }

    public static MetadataTracer forServer() {
        return new MetadataTracer(false);
    }

    public static MetadataTracer forAuto() {
        return new MetadataTracer();
    }

    public MetadataTracer() {
        this.isAuto = true;
        this.isClient = false;
    }

    public MetadataTracer(boolean isClient) {
        this.isAuto = false;
        this.isClient = isClient;
    }

    @Override
    public void beforeCall(Object[] args, InstanceMethodCaller caller) {
        boolean isClient = isClient();
        setBeforeCall(isClient ?
                        TraceContext.getCurrentTraceData().getClientSpan().getMetadata() :
                        TraceContext.getCurrentTraceData().getServiceSpan().getMetadata(),
                args, caller, isClient);

    }

    @Override
    public void afterCall(Object[] args, InstanceMethodCaller caller, Object result) {
        boolean isClient = isClient();
        setAfterCall(isClient ?
                        TraceContext.getCurrentTraceData().getClientSpan().getMetadata() :
                        TraceContext.getCurrentTraceData().getServiceSpan().getMetadata(),
                args, caller, result, isClient);
    }

    @Override
    public void callError(Object[] args, InstanceMethodCaller caller, Throwable error) {
        boolean isClient = isClient();
        setCallError(isClient ?
                        TraceContext.getCurrentTraceData().getClientSpan() :
                        TraceContext.getCurrentTraceData().getServiceSpan(),
                args, caller, error, isClient);
    }

    private void setBeforeCall(Metadata metadata, Object[] args, InstanceMethodCaller caller, boolean isClient) {
        metadata.putValue(MetadataProperties.CALL_ARGUMENTS, args);
        metadata.putValue(MetadataProperties.INSTANCE_METHOD_CALLER, caller);
        metadata.putValue(MetadataProperties.EVENT_TYPE, isClient ? ClientEventType.CALL_SERVICE : ServiceEventType.CALL_HANDLER);
    }

    private void setAfterCall(Metadata metadata, Object[] args, InstanceMethodCaller caller, Object result, boolean isClient) {
        metadata.putValue(MetadataProperties.CALL_RESULT, result);
        metadata.putValue(MetadataProperties.EVENT_TYPE, isClient ? ClientEventType.SERVICE_RESULT : ServiceEventType.HANDLER_RESULT);
    }

    private void setCallError(ContextSpan contextSpan, Object[] args, InstanceMethodCaller caller, Throwable error, boolean isClient) {
        ContextUtils.setCallError(contextSpan, error);
        contextSpan.getMetadata().putValue(MetadataProperties.EVENT_TYPE, isClient ? ClientEventType.ERROR : ServiceEventType.ERROR);
    }

    private boolean isClient() {
        return isAuto ? TraceContext.getCurrentTraceData().isClient() : isClient;
    }
}
