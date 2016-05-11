package com.rbkmoney.woody.api.event;

import com.rbkmoney.woody.api.trace.*;

/**
 * Created by vpankrashkin on 06.05.16.
 */
public abstract class Event {
    private final TraceData traceData;

    public Event(TraceData traceData) {
        this.traceData = traceData;
    }

    public TraceData getTraceData() {
        return traceData;
    }

    public ClientEventType getEventType() {
        return getActiveSpan().getMetadata().getValue(MetadataProperties.EVENT_TYPE);
    }

    public CallType getCallType() {
        return getActiveSpan().getMetadata().getValue(MetadataProperties.CALL_TYPE);
    }

    public String getCallName() {
        return getActiveSpan().getMetadata().getValue(MetadataProperties.CALL_NAME);
    }

    public Object[] getCallArguments() {
        return getActiveSpan().getMetadata().getValue(MetadataProperties.CALL_ARGUMENTS);
    }

    public Object getCallResult() {
        return getActiveSpan().getMetadata().getValue(MetadataProperties.CALL_RESULT);
    }

    public ErrorType getErrorType() {
        return getActiveSpan().getMetadata().getValue(MetadataProperties.ERROR_TYPE);
    }

    public String getErrorName() {
        return getActiveSpan().getMetadata().getValue(MetadataProperties.ERROR_NAME);
    }

    public String getErrorMessage() {
        return getActiveSpan().getMetadata().getValue(MetadataProperties.ERROR_MESSAGE);
    }

    public String getSpanId() {
        return getActiveSpan().getSpan().getId();
    }

    public String getParentId() {
        return getActiveSpan().getSpan().getParentId();
    }

    public String getTraceId() {
        return getActiveSpan().getSpan().getTraceId();
    }

    public long getTimeStamp() {
        return getActiveSpan().getSpan().getTimestamp();
    }

    public long getDuration() {
        return getActiveSpan().getSpan().getDuration();
    }

    public Endpoint getEndpoint() {
        return getActiveSpan().getMetadata().getValue(MetadataProperties.CALL_ENDPOINT);
    }

    public boolean isSuccessfullCall() {
        return !ContextUtils.hasCallErrors(getActiveSpan());

    }

    public abstract ContextSpan getActiveSpan();
}
