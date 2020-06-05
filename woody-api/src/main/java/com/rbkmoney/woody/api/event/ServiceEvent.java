package com.rbkmoney.woody.api.event;

import com.rbkmoney.woody.api.trace.ContextSpan;
import com.rbkmoney.woody.api.trace.TraceData;

public class ServiceEvent extends Event {
    public ServiceEvent(TraceData traceData) {
        super(traceData);
    }

    @Override
    public ServiceEventType getEventType() {
        return (ServiceEventType) super.getEventType();
    }

    @Override
    public ContextSpan getActiveSpan() {
        return getTraceData().getServiceSpan();
    }
}
