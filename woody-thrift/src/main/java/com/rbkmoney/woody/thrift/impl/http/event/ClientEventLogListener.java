package com.rbkmoney.woody.thrift.impl.http.event;

import com.rbkmoney.woody.api.event.ClientEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by vpankrashkin on 12.05.16.
 */
public class ClientEventLogListener implements ClientEventListener<THClientEvent> {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public void notifyEvent(THClientEvent event) {
        try {
            switch (event.getEventType()) {
                case CALL_SERVICE:
                    log.info("CLIENT Event: {}, Span [{}-{}-{}], [{}, Type: {}], Time: {}", event.getEventType(), event.getTraceId(), event.getSpanId(), event.getParentId(), event.getCallName(), event.getCallType(), event.getTimeStamp());
                    break;
                case CLIENT_SEND:
                    log.info("CLIENT Event: {}, Span [{}-{}-{}], Url: {}, Time: {}", event.getEventType(), event.getTraceId(), event.getSpanId(), event.getParentId(), event.getEndpoint().getStringValue(), event.getTimeStamp());
                    break;
                case CLIENT_RECEIVE:
                    log.info("CLIENT Event: {}, Span [{}-{}-{}], Status: {}, Time: {}", event.getEventType(), event.getTraceId(), event.getSpanId(), event.getParentId(), event.isSuccessfullCall() ? "ok" : "error", event.getTimeStamp());
                    break;
                case SERVICE_RESULT:
                    log.info("CLIENT Event: {}, Span [{}-{}-{}], Status: {}, Time: {}, Duration: {}", event.getEventType(), event.getTraceId(), event.getSpanId(), event.getParentId(), event.isSuccessfullCall() ? "ok" : "error", event.getTimeStamp(), event.getDuration());
                    break;
                case ERROR:
                    log.info("CLIENT Event: {}, Span [{}-{}-{}], ErrType: {}, TErrType: {}, ErrName: {},  Time: {}, Duration: {}", event.getEventType(), event.getTraceId(), event.getSpanId(), event.getParentId(), event.getErrorType(), event.getThriftErrorType(), event.getErrorName(), event.getTimeStamp(), event.getDuration());
                    break;
                default:
                    log.info("CLIENT Unknown error: {}", event);
                    break;
            }
        } catch (Exception e) {
            log.error("Event processing failed", e);
        }
    }
}
