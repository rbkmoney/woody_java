package com.rbkmoney.woody.thrift.impl.http.interceptor;

import com.rbkmoney.woody.api.event.ErrorType;
import com.rbkmoney.woody.api.interceptor.ResponseInterceptor;
import com.rbkmoney.woody.api.trace.*;
import com.rbkmoney.woody.thrift.impl.http.TErrorType;
import com.rbkmoney.woody.thrift.impl.http.THMetadataProperties;
import com.rbkmoney.woody.thrift.impl.http.transport.THttpHeader;
import com.rbkmoney.woody.thrift.impl.http.transport.TTransportErrorType;

import javax.servlet.http.HttpServletResponse;

/**
 * Created by vpankrashkin on 29.04.16.
 */
public class THSResponseInterceptor implements ResponseInterceptor {
    public static final String THRFIT_TRANSPORT_ERROR_MSG = "thrift transport error";
    public static final String THRFIT_PROTOCOL_ERROR_MSG = "thrift protocol error";
    public static final String UNKNOWN_PROVIDER_ERROR_MSG = "unknown provider error";
    public static final String BAD_REQUEST_HEADERS_MSG = "bad request headers";
    boolean isUseContext;

    public THSResponseInterceptor(boolean isUseContext) {
        this.isUseContext = isUseContext;
    }

    @Override
    public boolean interceptResponse(TraceData traceData, Object providerContext, Object... contextParams) {
        if (traceData.getServiceSpan().getMetadata().containsKey(THMetadataProperties.TH_TRANSPORT_RESPONSE_SET)) {
            return true;
        }

        HttpServletResponse response = null;
        if (isUseContext) {
            if (providerContext instanceof HttpServletResponse) {
                response = (HttpServletResponse) providerContext;
            }
        } else {
            response = ContextUtils.getMetadataParameter(traceData.getServiceSpan(), HttpServletResponse.class, THMetadataProperties.TH_TRANSPORT_RESPONSE);
        }

        if (response == null) {
            return interceptError(traceData, "Unknown type:" + providerContext.getClass());
        }

        if (response.isCommitted()) {
            return true;
        }

        return interceptHttpResponse(traceData.getServiceSpan(), response);

    }

    private boolean interceptHttpResponse(ServiceSpan serviceSpan, HttpServletResponse response) {
        response.addHeader(THttpHeader.TRACE_ID.getKeyValue(), serviceSpan.getSpan().getTraceId());
        response.addHeader(THttpHeader.SPAN_ID.getKeyValue(), serviceSpan.getSpan().getId());
        response.addHeader(THttpHeader.PARENT_ID.getKeyValue(), serviceSpan.getSpan().getParentId());

        int responseStatus;
        String errLogicValue = null;
        String errThriftValue = null;
        Metadata metadata = serviceSpan.getMetadata();
        ErrorType errType = ContextUtils.getMetadataParameter(serviceSpan, ErrorType.class, MetadataProperties.ERROR_TYPE);
        if (errType != null) {
            switch (errType) {
                case APPLICATION_KNOWN_ERROR:
                    responseStatus = 200;
                    errLogicValue = metadata.getValue(MetadataProperties.ERROR_NAME);
                    break;
                case PROVIDER_ERROR:
                    TErrorType tErrorType = ContextUtils.getMetadataParameter(serviceSpan, TErrorType.class, THMetadataProperties.TH_ERROR_TYPE);
                    if (tErrorType != null) {
                        switch (tErrorType) {
                            case UNKNOWN_CALL:
                                responseStatus = 405;
                                errThriftValue = "Unknown method:" + metadata.getValue(MetadataProperties.CALL_NAME);
                                break;
                            case TRANSPORT:
                                TTransportErrorType tTransportErrorType = ContextUtils.getMetadataParameter(serviceSpan, TTransportErrorType.class, THMetadataProperties.TH_ERROR_SUBTYPE);
                                if (tTransportErrorType != null) {
                                    switch (tTransportErrorType) {
                                        case BAD_TRACE_HEADERS:
                                        case BAD_CONTENT_TYPE:
                                            responseStatus = 403;
                                            errThriftValue = BAD_REQUEST_HEADERS_MSG;
                                            break;
                                        default:
                                            responseStatus = 403;
                                            errThriftValue = THRFIT_TRANSPORT_ERROR_MSG;
                                    }
                                } else {
                                    responseStatus = 403;
                                    errThriftValue = THRFIT_TRANSPORT_ERROR_MSG;
                                }
                                break;
                            case PROTOCOL:
                                responseStatus = 406;
                                errThriftValue = THRFIT_PROTOCOL_ERROR_MSG;
                                break;
                            case UNKNOWN:
                            default:
                                responseStatus = 410;
                                errThriftValue = UNKNOWN_PROVIDER_ERROR_MSG;
                                break;
                        }
                    } else {
                        responseStatus = 410;
                        errThriftValue = UNKNOWN_PROVIDER_ERROR_MSG;
                    }
                    break;
                case APPLICATION_UNKNOWN_ERROR:
                case OTHER:
                default:
                    responseStatus = 500;
                    errThriftValue = metadata.getValue(MetadataProperties.ERROR_NAME);
                    break;
            }
        } else {
            responseStatus = 200;
        }

        if (errLogicValue != null) {
            response.addHeader(THttpHeader.ERROR_LOGIC.getKeyValue(), errLogicValue);
        }

        if (errThriftValue != null) {
            response.addHeader(THttpHeader.ERROR_THRIFT.getKeyValue(), errThriftValue);
        }

        response.setStatus(responseStatus);

        serviceSpan.getMetadata().putValue(THMetadataProperties.TH_TRANSPORT_RESPONSE_SET, true);

        return true;
    }

    private boolean interceptError(TraceData traceData, String message) {
        return interceptError(traceData, new RuntimeException(message));
    }

    private boolean interceptError(TraceData traceData, Throwable cause) {
        ContextUtils.setInterceptionError(traceData.getServiceSpan(), cause);
        return false;
    }

}
