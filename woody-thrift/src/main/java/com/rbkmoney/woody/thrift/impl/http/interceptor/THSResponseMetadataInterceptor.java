package com.rbkmoney.woody.thrift.impl.http.interceptor;

import com.rbkmoney.woody.api.interceptor.ResponseInterceptor;
import com.rbkmoney.woody.api.trace.TraceData;
import com.rbkmoney.woody.thrift.impl.http.error.THErrorMapProcessor;

public class THSResponseMetadataInterceptor implements ResponseInterceptor {
    private final THErrorMapProcessor errorMapProcessor;

    public THSResponseMetadataInterceptor(THErrorMapProcessor errorMapProcessor) {
        this.errorMapProcessor = errorMapProcessor;
    }

    @Override
    public boolean interceptResponse(TraceData traceData, Object providerContext, Object... contextParams) {
        errorMapProcessor.processMapToDef(traceData);
        return true;
    }
}
