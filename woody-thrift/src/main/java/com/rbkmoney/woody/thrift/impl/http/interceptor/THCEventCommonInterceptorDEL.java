package com.rbkmoney.woody.thrift.impl.http.interceptor;

import com.rbkmoney.woody.api.event.ClientEventType;
import com.rbkmoney.woody.api.interceptor.CommonInterceptor;
import com.rbkmoney.woody.api.trace.MetadataProperties;
import com.rbkmoney.woody.api.trace.TraceData;

/**
 * Created by vpankrashkin on 06.05.16.
 */
public class THCEventCommonInterceptorDEL implements CommonInterceptor {
    private final Runnable requestListener;
    private final Runnable responseListener;

    public THCEventCommonInterceptorDEL(Runnable requestListener, Runnable responseListener) {
        this.requestListener = requestListener;
        this.responseListener = responseListener;
    }

    @Override
    public boolean interceptRequest(TraceData traceData, Object providerContext, Object... contextParams) {
        traceData.getClientSpan().getMetadata().putValue(MetadataProperties.EVENT_TYPE, ClientEventType.CLIENT_SEND);
        requestListener.run();
        return true;
    }

    @Override
    public boolean interceptResponse(TraceData traceData, Object providerContext, Object... contextParams) {
        traceData.getClientSpan().getMetadata().putValue(MetadataProperties.EVENT_TYPE, ClientEventType.CLIENT_RECEIVE);
        responseListener.run();
        return true;
    }
}
