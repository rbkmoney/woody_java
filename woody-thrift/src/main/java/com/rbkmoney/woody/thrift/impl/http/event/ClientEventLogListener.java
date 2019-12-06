package com.rbkmoney.woody.thrift.impl.http.event;

import com.rbkmoney.woody.api.event.ClientEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

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
                    log.info("CLIENT Event: {}, [{}, Type: {}], Time: {}", event.getEventType(), event.getCallName(), event.getCallType(), event.getTimeStamp());
                    break;
                case CLIENT_SEND:
                    log.info("CLIENT Event: {}, Url: {}, Time: {}", event.getEventType(), event.getEndpoint().getStringValue(), event.getTimeStamp());
                    break;
                case CLIENT_RECEIVE:
                    log.info("CLIENT Event: {}, Status: {}, Time: {}", event.getEventType(), event.isSuccessfulCall() ? "ok" : "error", event.getTimeStamp());
                    break;
                case SERVICE_RESULT:
                    log.info("CLIENT Event: {}, Status: {}, Time: {}, Duration: {}", event.getEventType(), event.isSuccessfulCall() ? "ok" : "error", event.getTimeStamp(), event.getDuration());
                    break;
                case ERROR:
                    log.error("CLIENT Event: {}, ErrType: {}, TErrType: {}, ErrName: {},  Time: {}, Duration: {}", event.getEventType(), event.getErrorDefinition(), event.getThriftErrorType(), Optional.ofNullable(event.getErrorDefinition()).map(ed -> ed.getErrorName()).orElse(""), event.getTimeStamp(), event.getDuration());
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
