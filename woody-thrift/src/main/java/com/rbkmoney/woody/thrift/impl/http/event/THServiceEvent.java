package com.rbkmoney.woody.thrift.impl.http.event;

import com.rbkmoney.woody.api.event.ServiceEvent;
import com.rbkmoney.woody.api.trace.ContextUtils;
import com.rbkmoney.woody.api.trace.TraceData;
import com.rbkmoney.woody.thrift.impl.http.TErrorType;
import com.rbkmoney.woody.thrift.impl.http.THMetadataProperties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by vpankrashkin on 06.05.16.
 */
public class THServiceEvent extends ServiceEvent {
    public THServiceEvent(TraceData traceData) {
        super(traceData);
    }

    public Integer getThriftCallMsgType() {
        return getActiveSpan().getMetadata().getValue(THMetadataProperties.TH_CALL_MSG_TYPE);
    }

    public Integer getThiftCallResultMsgType() {
        return getActiveSpan().getMetadata().getValue(THMetadataProperties.TH_CALL_RESULT_MSG_TYPE);
    }

    public TErrorType getThriftErrorType() {
        return getActiveSpan().getMetadata().getValue(THMetadataProperties.TH_ERROR_TYPE);
    }

    public Integer getThriftResponseStatus() {
        return getActiveSpan().getMetadata().getValue(THMetadataProperties.TH_RESPONSE_STATUS);
    }

    public String getThriftResponseMessage() {
        return getActiveSpan().getMetadata().getValue(THMetadataProperties.TH_RESPONSE_MESSAGE);
    }

    public HttpServletRequest getTransportRequest() {
        return ContextUtils.getMetadataParameter(getActiveSpan(), HttpServletRequest.class, THMetadataProperties.TH_TRANSPORT_REQUEST);
    }

    public HttpServletResponse getTransportResponse() {
        return ContextUtils.getMetadataParameter(getActiveSpan(), HttpServletResponse.class, THMetadataProperties.TH_TRANSPORT_RESPONSE);
    }
}
