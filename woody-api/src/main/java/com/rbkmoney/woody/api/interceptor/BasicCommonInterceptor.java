package com.rbkmoney.woody.api.interceptor;

import com.rbkmoney.woody.api.trace.TraceData;

/**
 * Created by vpankrashkin on 04.05.16.
 */
public class BasicCommonInterceptor<ReqProvider, RespProvider> implements CommonInterceptor<ReqProvider, RespProvider> {
    private RequestInterceptor<ReqProvider> requestInterceptor;
    private ResponseInterceptor<RespProvider> responseInterceptor;

    public BasicCommonInterceptor(RequestInterceptor<ReqProvider> requestInterceptor, ResponseInterceptor<RespProvider> responseInterceptor) {
        this.requestInterceptor = requestInterceptor == null ? new EmptyCommonInterceptor() : requestInterceptor;
        this.responseInterceptor = responseInterceptor == null ? new EmptyCommonInterceptor() : responseInterceptor;
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
