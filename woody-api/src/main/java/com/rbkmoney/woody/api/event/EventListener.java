package com.rbkmoney.woody.api.event;

import com.rbkmoney.woody.api.trace.TraceData;

/**
 * Created by vpankrashkin on 25.04.16.
 */
public interface EventListener<EvnT, ErrT> {
    void notifyEvent(EvnT eventType, TraceData traceData);

    void notifyError(ErrT errorType, TraceData traceData);
}
