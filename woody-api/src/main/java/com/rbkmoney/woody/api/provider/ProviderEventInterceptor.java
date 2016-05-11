package com.rbkmoney.woody.api.provider;

import com.rbkmoney.woody.api.event.ClientEventType;
import com.rbkmoney.woody.api.event.ServiceEventType;
import com.rbkmoney.woody.api.interceptor.CommonInterceptor;
import com.rbkmoney.woody.api.trace.MetadataProperties;
import com.rbkmoney.woody.api.trace.TraceData;

/**
 * Created by vpankrashkin on 27.04.16.
 */
public class ProviderEventInterceptor<ReqProvider, RespProvider> implements CommonInterceptor<ReqProvider, RespProvider> {
    private final Runnable reqListener;
    private final Runnable respListener;

    public ProviderEventInterceptor(Runnable reqListener, Runnable respListener) {
        this.reqListener = reqListener != null ? reqListener : () -> {
        };
        this.respListener = respListener != null ? respListener : () -> {
        };
    }

    @Override
    public boolean interceptRequest(TraceData traceData, ReqProvider providerContext, Object... contextParams) {
        traceData.getActiveSpan().getMetadata().putValue(MetadataProperties.EVENT_TYPE, traceData.isClient() ? ClientEventType.CALL_SERVICE : ServiceEventType.SERVICE_RECEIVE);
        reqListener.run();
        return true;
    }

    @Override
    public boolean interceptResponse(TraceData traceData, RespProvider providerContext, Object... contextParams) {
        traceData.getActiveSpan().getMetadata().putValue(MetadataProperties.EVENT_TYPE, traceData.isClient() ? ClientEventType.SERVICE_RESULT : ServiceEventType.HANDLER_RESULT);
        respListener.run();
        return true;
    }

}
