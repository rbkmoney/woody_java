package com.rbkmoney.woody.api.event;

import com.rbkmoney.woody.api.trace.ContextSpan;
import com.rbkmoney.woody.api.trace.TraceData;

/**
 * Created by vpankrashkin on 06.05.16.
 */
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
