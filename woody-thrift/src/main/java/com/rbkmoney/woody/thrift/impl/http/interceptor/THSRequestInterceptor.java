package com.rbkmoney.woody.thrift.impl.http.interceptor;

import com.rbkmoney.woody.api.interceptor.RequestInterceptor;
import com.rbkmoney.woody.api.trace.*;
import com.rbkmoney.woody.thrift.impl.http.THMetadataProperties;
import com.rbkmoney.woody.thrift.impl.http.transport.THttpHeader;
import com.rbkmoney.woody.thrift.impl.http.transport.TTransportErrorType;
import com.rbkmoney.woody.thrift.impl.http.transport.UrlStringEndpoint;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by vpankrashkin on 29.04.16.
 */
public class THSRequestInterceptor implements RequestInterceptor {
    @Override
    public boolean interceptRequest(TraceData traceData, Object providerContext, Object... contextParams) {
        if (providerContext instanceof HttpServletRequest) {
            return interceptHttpRequest(traceData, (HttpServletRequest) providerContext, contextParams);
        }
        return interceptError(traceData, "Unknown type:" + providerContext.getClass());
    }

    protected boolean interceptHttpRequest(TraceData traceData, HttpServletRequest request, Object... contextParams) {
        THttpHeader errHeader = setSpanHeaders(traceData.getServiceSpan(), request);
        if (errHeader != null) {
            return interceptError(traceData, new THRequestInterceptionException(TTransportErrorType.BAD_TRACE_HEADERS));
        }
        extendMetadata(traceData.getServiceSpan(), request, contextParams);
        return true;
    }


    private THttpHeader setSpanHeaders(ServiceSpan serviceSpan, HttpServletRequest request) {
        Span span = serviceSpan.getSpan();
        String header = getSpanHeader(THttpHeader.TRACE_ID.getKeyValue(), request);
        if (header == null) {
            return THttpHeader.TRACE_ID;
        }
        span.setTraceId(header);

        header = getSpanHeader(THttpHeader.SPAN_ID.getKeyValue(), request);
        if (header == null) {
            return THttpHeader.SPAN_ID;
        }
        span.setId(header);

        header = getSpanHeader(THttpHeader.PARENT_ID.getKeyValue(), request);
        span.setParentId(header);
        return null;
    }

    private void extendMetadata(ServiceSpan serviceSpan, HttpServletRequest request, Object... contextParams) {
        String queryString = request.getQueryString();
        StringBuffer sb = request.getRequestURL();
        if (queryString != null) {
            sb.append('?').append(request.getQueryString());
        }
        serviceSpan.getMetadata().putValue(MetadataProperties.CALL_ENDPOINT, new UrlStringEndpoint(sb.toString()));
        HttpServletResponse response = ContextUtils.getContextParameter(HttpServletResponse.class, contextParams, 0);
        if (response != null) {
            serviceSpan.getMetadata().putValue(THMetadataProperties.TH_TRANSPORT_RESPONSE, response);
        }
    }

    private String getSpanHeader(String name, HttpServletRequest request) {
        String value = request.getHeader(name);
        if (value == null || value.length() == 0) {
            return null;
        }
        return value;
    }


    private boolean interceptError(TraceData traceData, String message) {
        return interceptError(traceData, new RuntimeException(message));
    }

    private boolean interceptError(TraceData traceData, Throwable t) {
        ContextUtils.setInterceptionError(traceData.getServiceSpan(), t);
        return false;
    }

}
