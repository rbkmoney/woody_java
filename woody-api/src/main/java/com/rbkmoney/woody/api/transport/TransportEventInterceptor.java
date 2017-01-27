package com.rbkmoney.woody.api.transport;

import com.rbkmoney.woody.api.event.ClientEventType;
import com.rbkmoney.woody.api.event.ServiceEventType;
import com.rbkmoney.woody.api.interceptor.CommonInterceptor;
import com.rbkmoney.woody.api.trace.MetadataProperties;
import com.rbkmoney.woody.api.trace.TraceData;

/**
 * Created by vpankrashkin on 27.04.16.
 */
public class TransportEventInterceptor implements CommonInterceptor{
    private final Runnable reqListener;
    private final Runnable respListener;
    private final Runnable errListener;

    public TransportEventInterceptor(Runnable reqListener, Runnable respListener, Runnable errListener) {
        this.reqListener = reqListener != null ? reqListener : () -> {
        };
        this.respListener = respListener != null ? respListener : () -> {
        };
        this.errListener = errListener != null ? errListener : () -> {
        };
    }

    @Override
    public boolean interceptRequest(TraceData traceData, Object providerContext, Object... contextParams) {
        traceData.getActiveSpan().getMetadata().putValue(MetadataProperties.EVENT_TYPE, traceData.isClient() ? ClientEventType.CLIENT_SEND : ServiceEventType.SERVICE_RECEIVE);
        reqListener.run();
        return true;
    }

    @Override
    public boolean interceptResponse(TraceData traceData, Object providerContext, Object... contextParams) {
        traceData.getActiveSpan().getMetadata().putValue(MetadataProperties.EVENT_TYPE, traceData.isClient() ? ClientEventType.CLIENT_RECEIVE : ServiceEventType.SERVICE_RESULT);
        respListener.run();
        return true;
    }

    @Override
    public boolean interceptError(TraceData traceData, Throwable t, boolean isClient) {
        errListener.run();
        return (CommonInterceptor.super.interceptError(traceData, t, isClient));
    }
}
