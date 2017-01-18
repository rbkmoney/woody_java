package com.rbkmoney.woody.thrift.impl.http.interceptor.ext;

import com.rbkmoney.woody.api.interceptor.ext.ExtensionContext;
import com.rbkmoney.woody.api.trace.ContextUtils;
import com.rbkmoney.woody.api.trace.TraceData;
import com.rbkmoney.woody.thrift.impl.http.THMetadataProperties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by vpankrashkin on 14.12.16.
 */
public class THSExtensionContext extends ExtensionContext {
    public THSExtensionContext(TraceData traceData, Object providerContext, Object[] contextParameters) {
        super(traceData, providerContext, contextParameters);
    }

    public HttpServletRequest getProviderRequest() {
        Object providerContext = getProviderContext();
        if (providerContext instanceof HttpServletRequest) {
            return (HttpServletRequest) providerContext;
        }
        throw new IllegalArgumentException("Unknown type:" + providerContext.getClass());
    }

    public HttpServletResponse getProviderResponse() {
        HttpServletResponse response = null;
        Object providerContext = getProviderContext();
        if (providerContext instanceof HttpServletResponse) {
            response = (HttpServletResponse) providerContext;
        }
        if (response == null) {
            response = ContextUtils.getContextParameter(HttpServletResponse.class, getContextParameters(), 0);
        }
        if (response == null) {
            response = ContextUtils.getMetadataParameter(getTraceData().getServiceSpan(), HttpServletResponse.class, THMetadataProperties.TH_TRANSPORT_RESPONSE);
        }

        if (response == null) {
            throw new IllegalArgumentException("Unknown type:" + providerContext.getClass() + "|" + ContextUtils.getContextParameter(Object.class, getContextParameters(), 0));
        }
        return response;
    }

    public void setResponseHeader(String key, String value) {
        getProviderResponse().setHeader(key, value);
    }
}
