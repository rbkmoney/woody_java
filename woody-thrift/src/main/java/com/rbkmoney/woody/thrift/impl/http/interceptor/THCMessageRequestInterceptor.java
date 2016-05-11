package com.rbkmoney.woody.thrift.impl.http.interceptor;

import com.rbkmoney.woody.api.event.CallType;
import com.rbkmoney.woody.api.interceptor.RequestInterceptor;
import com.rbkmoney.woody.api.trace.Metadata;
import com.rbkmoney.woody.api.trace.MetadataProperties;
import com.rbkmoney.woody.api.trace.TraceData;
import com.rbkmoney.woody.thrift.impl.http.THMetadataProperties;
import org.apache.thrift.protocol.TMessage;
import org.apache.thrift.protocol.TMessageType;

/**
 * Created by vpankrashkin on 05.05.16.
 */
public class THCMessageRequestInterceptor implements RequestInterceptor<TMessage> {
    private final Runnable eventListener;

    public THCMessageRequestInterceptor() {
        this(null);
    }

    public THCMessageRequestInterceptor(Runnable eventListener) {
        this.eventListener = eventListener == null ? () -> {
        } : eventListener;
    }

    @Override
    public boolean interceptRequest(TraceData traceData, TMessage providerContext, Object... contextParams) {
        Metadata metadata = traceData.getClientSpan().getMetadata();
        metadata.putValue(MetadataProperties.CALL_NAME, providerContext.name);
        metadata.putValue(MetadataProperties.CALL_TYPE, providerContext.type == TMessageType.ONEWAY ? CallType.CAST : CallType.CALL);
        metadata.putValue(THMetadataProperties.TH_CALL_MSG_TYPE, providerContext.type);
        eventListener.run();
        return true;
    }
}
