package com.rbkmoney.woody.thrift.impl.http.interceptor;

import com.rbkmoney.woody.api.interceptor.ResponseInterceptor;
import com.rbkmoney.woody.api.trace.Metadata;
import com.rbkmoney.woody.api.trace.TraceData;
import com.rbkmoney.woody.thrift.impl.http.THMetadataProperties;
import org.apache.thrift.protocol.TMessage;

/**
 * Created by vpankrashkin on 05.05.16.
 */
public class THSMessageResponseInterceptor implements ResponseInterceptor<TMessage> {
    private final Runnable eventListener;

    public THSMessageResponseInterceptor() {
        this(() -> {
        });
    }

    public THSMessageResponseInterceptor(Runnable eventListener) {
        this.eventListener = eventListener;
    }

    @Override
    public boolean interceptResponse(TraceData traceData, TMessage providerContext, Object... contextParams) {
        Metadata metadata = traceData.getClientSpan().getMetadata();
        metadata.putValue(THMetadataProperties.TH_CALL_RESULT_MSG_TYPE, providerContext.type);
        eventListener.run();
        return true;
    }
}
