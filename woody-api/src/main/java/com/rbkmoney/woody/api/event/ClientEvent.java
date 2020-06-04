package com.rbkmoney.woody.api.event;

import com.rbkmoney.woody.api.trace.ContextSpan;
import com.rbkmoney.woody.api.trace.TraceData;

public class ClientEvent extends Event {

    public ClientEvent(TraceData traceData) {
        super(traceData);
    }

    @Override
    public ClientEventType getEventType() {
        return (ClientEventType) super.getEventType();
    }

    @Override
    public ContextSpan getActiveSpan() {
        return getTraceData().getClientSpan();
    }
}
