package com.rbkmoney.woody.api.transport.interceptor;

import com.rbkmoney.woody.api.event.ClientEventType;
import com.rbkmoney.woody.api.event.ServiceEventType;
import com.rbkmoney.woody.api.trace.AbstractSpan;
import com.rbkmoney.woody.api.trace.MetadataProperties;
import com.rbkmoney.woody.api.trace.TraceData;
import com.rbkmoney.woody.api.trace.context.TraceContext;

/**
 * Created by vpankrashkin on 27.04.16.
 */
public class WrappedRequestInterceptor<Context extends AbstractSpan, Transport> implements RequestInterceptor<Context, Transport> {
    private final RequestInterceptor interceptor;
    private final Runnable listener;

    public WrappedRequestInterceptor(RequestInterceptor interceptor, Runnable listener) {
        this.interceptor = interceptor;
        this.listener = listener;
    }

    @Override
    public boolean interceptRequest(Context context, Transport spanContext) {
        TraceData traceContext = TraceContext.getCurrentTraceData();
        boolean isClient = traceContext.isClient();

        if (interceptor.interceptRequest(context, spanContext)) {
            traceContext.getActiveSpan().getMetadata().putValue(MetadataProperties.EVENT_TYPE, isClient ? ClientEventType.CLIENT_RECEIVE : ServiceEventType.SERVICE_RECEIVE);
            listener.run();
            return true;
        } else {
            traceContext.getActiveSpan().getMetadata().putValue(MetadataProperties.EVENT_TYPE, isClient ? ClientEventType.ERROR : ServiceEventType.ERROR);
            return false;
        }
    }
}
