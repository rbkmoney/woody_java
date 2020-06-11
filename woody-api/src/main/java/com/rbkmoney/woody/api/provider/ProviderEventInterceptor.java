package com.rbkmoney.woody.api.provider;

import com.rbkmoney.woody.api.event.ClientEventType;
import com.rbkmoney.woody.api.event.ServiceEventType;
import com.rbkmoney.woody.api.interceptor.CommonInterceptor;
import com.rbkmoney.woody.api.trace.MetadataProperties;
import com.rbkmoney.woody.api.trace.TraceData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProviderEventInterceptor implements CommonInterceptor {

    private static final Logger LOG = LoggerFactory.getLogger(ProviderEventInterceptor.class);

    private final Runnable reqListener;
    private final Runnable respListener;

    public ProviderEventInterceptor(Runnable reqListener, Runnable respListener) {
        this.reqListener = reqListener != null ? reqListener : () -> {
        };
        this.respListener = respListener != null ? respListener : () -> {
        };
    }

    @Override
    public boolean interceptRequest(TraceData traceData, Object providerContext, Object... contextParams) {
        LOG.trace("Intercept request providerEvent");
        traceData.getActiveSpan().getMetadata().putValue(MetadataProperties.EVENT_TYPE, traceData.isClient() ? ClientEventType.CALL_SERVICE : ServiceEventType.SERVICE_RECEIVE);
        reqListener.run();
        return true;
    }

    @Override
    public boolean interceptResponse(TraceData traceData, Object providerContext, Object... contextParams) {
        LOG.trace("Intercept response providerEvent");
        traceData.getActiveSpan().getMetadata().putValue(MetadataProperties.EVENT_TYPE, traceData.isClient() ? ClientEventType.SERVICE_RESULT : ServiceEventType.HANDLER_RESULT);
        respListener.run();
        return true;
    }

}
