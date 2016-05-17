package com.rbkmoney.woody.thrift.impl.http.event;

import com.rbkmoney.woody.api.event.ServiceEvent;
import com.rbkmoney.woody.api.event.ServiceEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by vpankrashkin on 12.05.16.
 */
public class ServiceEventLogListener implements ServiceEventListener {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public void notifyEvent(ServiceEvent event1) {
        try {
            THServiceEvent event = (THServiceEvent) event1;
            switch (event.getEventType()) {
                case CALL_HANDLER:
                    log.info("SERVER Event: {}, Span [{}-{}-{}], [{}, Type: {}], Time: {}", event.getEventType(), event.getTraceId(), event.getSpanId(), event.getParentId(), event.getCallName(), event.getCallType(), event.getTimeStamp());
                    break;
                case HANDLER_RESULT:
                    log.info("SERVER Event: {}, Span [{}-{}-{}], Status: {}, Time: {}", event.getEventType(), event.getTraceId(), event.getSpanId(), event.getParentId(), event.isSuccessfullCall() ? "ok" : "error", event.getTimeStamp());
                    break;
                case SERVICE_RECEIVE:
                    log.info("SERVER Event: {}, Span [{}-{}-{}], Status: {}, Url: {}, Time: {}", event.getEventType(), event.getTraceId(), event.getSpanId(), event.getParentId(), event.isSuccessfullCall() ? "ok" : "error", event.getEndpoint().getStringValue(), event.getTimeStamp());
                    break;
                case SERVICE_RESULT:
                    log.info("SERVER Event: {}, Span [{}-{}-{}], Status: {}, Time: {}, Duration: {}", event.getEventType(), event.getTraceId(), event.getSpanId(), event.getParentId(), event.isSuccessfullCall() ? "ok" : "error", event.getTimeStamp(), event.getDuration());
                    break;
                case ERROR:
                    log.info("SERVER Event: {}, Span [{}-{}-{}], ErrType: {}, TErrType: {}, ErrName: {},  Time: {}", event.getEventType(), event.getTraceId(), event.getSpanId(), event.getParentId(), event.getErrorType(), event.getThriftErrorType(), event.getErrorName(), event.getTimeStamp());
                    break;
                default:
                    log.info("SERVER Unknown error: {}", event);
                    break;

            }
        } catch (Exception e) {
            log.error("Failed to process event", e);
        }

    }
}
