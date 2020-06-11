package com.rbkmoney.woody.api.interceptor;

import com.rbkmoney.woody.api.trace.TraceData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContainerCommonInterceptor implements CommonInterceptor {

    private static final Logger LOG = LoggerFactory.getLogger(ContainerCommonInterceptor.class);

    private RequestInterceptor requestInterceptor;
    private ResponseInterceptor responseInterceptor;

    public ContainerCommonInterceptor(RequestInterceptor requestInterceptor, ResponseInterceptor responseInterceptor) {
        this.requestInterceptor = requestInterceptor != null ? requestInterceptor : new EmptyCommonInterceptor();
        this.responseInterceptor = responseInterceptor != null ? responseInterceptor : new EmptyCommonInterceptor();
    }

    @Override
    public boolean interceptRequest(TraceData traceData, Object providerContext, Object... contextParams) {
        LOG.trace("Request interceptor container");
        return requestInterceptor.interceptRequest(traceData, providerContext, contextParams);
    }

    @Override
    public boolean interceptResponse(TraceData traceData, Object providerContext, Object... contextParams) {
        LOG.trace("Response interceptor container");
        return responseInterceptor.interceptResponse(traceData, providerContext, contextParams);
    }

}
