package com.rbkmoney.woody.api.event;

import com.rbkmoney.woody.api.trace.TraceData;

/**
 * Created by vpankrashkin on 25.04.16.
 */
public interface ServiceEventListener extends EventListener<ServiceEventType, ErrorType> {
    void notifyEvent(ServiceEventType eventType, TraceData traceData);

    void notifyError(ErrorType errorType, TraceData traceData);
}
