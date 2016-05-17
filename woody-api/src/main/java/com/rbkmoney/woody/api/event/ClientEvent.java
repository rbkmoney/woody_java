package com.rbkmoney.woody.api.event;

import com.rbkmoney.woody.api.trace.ContextSpan;
import com.rbkmoney.woody.api.trace.TraceData;

/**
 * Created by vpankrashkin on 06.05.16.
 */
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
