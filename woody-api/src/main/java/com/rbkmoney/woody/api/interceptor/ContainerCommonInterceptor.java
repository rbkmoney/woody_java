package com.rbkmoney.woody.api.interceptor;

import com.rbkmoney.woody.api.trace.TraceData;

/**
 * Created by vpankrashkin on 04.05.16.
 */
public class ContainerCommonInterceptor<ReqProvider, RespProvider> implements CommonInterceptor<ReqProvider, RespProvider> {
    private RequestInterceptor<ReqProvider> requestInterceptor;
    private ResponseInterceptor<RespProvider> responseInterceptor;

    public ContainerCommonInterceptor(RequestInterceptor<ReqProvider> requestInterceptor, ResponseInterceptor<RespProvider> responseInterceptor) {
        this.requestInterceptor = requestInterceptor != null ? requestInterceptor : new EmptyCommonInterceptor();
        this.responseInterceptor = responseInterceptor != null ? responseInterceptor : new EmptyCommonInterceptor();
    }

    @Override
    public boolean interceptRequest(TraceData traceData, ReqProvider providerContext, Object... contextParams) {
        return requestInterceptor.interceptRequest(traceData, providerContext, contextParams);
    }

    @Override
    public boolean interceptResponse(TraceData traceData, RespProvider providerContext, Object... contextParams) {
        return responseInterceptor.interceptResponse(traceData, providerContext, contextParams);
    }

}
