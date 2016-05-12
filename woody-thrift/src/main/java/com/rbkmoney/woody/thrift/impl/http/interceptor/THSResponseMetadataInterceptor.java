package com.rbkmoney.woody.thrift.impl.http.interceptor;

import com.rbkmoney.woody.api.interceptor.ResponseInterceptor;
import com.rbkmoney.woody.api.trace.TraceData;
import com.rbkmoney.woody.thrift.impl.http.THErrorMetadataExtender;

/**
 * Created by vpankrashkin on 11.05.16.
 */
public class THSResponseMetadataInterceptor implements ResponseInterceptor {
    private final THErrorMetadataExtender metadataExtender;

    public THSResponseMetadataInterceptor(THErrorMetadataExtender metadataExtender) {
        this.metadataExtender = metadataExtender;
    }

    @Override
    public boolean interceptResponse(TraceData traceData, Object providerContext, Object... contextParams) {
        metadataExtender.extendServiceError(traceData);
        return true;
    }
}
