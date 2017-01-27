package com.rbkmoney.woody.api.event;

import com.rbkmoney.woody.api.flow.error.WErrorDefinition;
import com.rbkmoney.woody.api.trace.*;

import static com.rbkmoney.woody.api.trace.MetadataProperties.*;

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

    public Object getEventType() {
        return getActiveSpan().getMetadata().getValue(EVENT_TYPE);
    }

    public CallType getCallType() {
        return getActiveSpan().getMetadata().getValue(CALL_TYPE);
    }

    public String getCallName() {
        return getActiveSpan().getMetadata().getValue(CALL_NAME);
    }

    public Object[] getCallArguments() {
        return getActiveSpan().getMetadata().getValue(CALL_ARGUMENTS);
    }

    public Object getCallResult() {
        return getActiveSpan().getMetadata().getValue(CALL_RESULT);
    }

    public WErrorDefinition getErrorDefinition() {
        return getActiveSpan().getMetadata().getValue(ERROR_DEFINITION);
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
        return getActiveSpan().getMetadata().getValue(CALL_ENDPOINT);
    }

    public boolean isSuccessfullCall() {
        return !ContextUtils.hasCallErrors(getActiveSpan());

    }

    public abstract ContextSpan getActiveSpan();
}
