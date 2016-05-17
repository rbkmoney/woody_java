package com.rbkmoney.woody.thrift.impl.http.event;

import com.rbkmoney.woody.api.event.ClientEvent;
import com.rbkmoney.woody.api.trace.TraceData;
import com.rbkmoney.woody.thrift.impl.http.TErrorType;
import com.rbkmoney.woody.thrift.impl.http.THMetadataProperties;

/**
 * Created by vpankrashkin on 06.05.16.
 */
public class THClientEvent extends ClientEvent {
    public THClientEvent(TraceData traceData) {
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
}
