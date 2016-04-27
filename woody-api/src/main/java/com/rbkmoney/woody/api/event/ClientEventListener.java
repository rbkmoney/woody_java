package com.rbkmoney.woody.api.event;

import com.rbkmoney.woody.api.trace.TraceData;

/**
 * Created by vpankrashkin on 22.04.16.
 */
public interface ClientEventListener extends EventListener<ClientEventType, ErrorType> {
    void notifyEvent(ClientEventType eventType, TraceData traceData);

    void notifyError(ErrorType errorType, TraceData traceData);
}
