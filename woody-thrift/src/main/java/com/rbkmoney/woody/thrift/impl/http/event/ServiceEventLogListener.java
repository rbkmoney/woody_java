package com.rbkmoney.woody.thrift.impl.http.event;

import com.rbkmoney.woody.api.event.ServiceEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Created by vpankrashkin on 12.05.16.
 */
public class ServiceEventLogListener implements ServiceEventListener<THServiceEvent> {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public void notifyEvent(THServiceEvent event) {
        try {
            switch (event.getEventType()) {
                case CALL_HANDLER:
                    log.info("SERVER Event: {}, [{}, Type: {}], Time: {}", event.getEventType(), event.getCallName(), event.getCallType(), event.getTimeStamp());
                    break;
                case HANDLER_RESULT:
                    log.info("SERVER Event: {}, Status: {}, Time: {}", event.getEventType(), event.isSuccessfulCall() ? "ok" : "error", event.getTimeStamp());
                    break;
                case SERVICE_RECEIVE:
                    log.info("SERVER Event: {}, Status: {}, Url: {}, Time: {}", event.getEventType(), event.isSuccessfulCall() ? "ok" : "error", event.getEndpoint().getStringValue(), event.getTimeStamp());
                    break;
                case SERVICE_RESULT:
                    log.info("SERVER Event: {}, Status: {}, Time: {}, Duration: {}", event.getEventType(), event.isSuccessfulCall() ? "ok" : "error", event.getTimeStamp(), event.getDuration());
                    break;
                case ERROR:
                    log.info("SERVER Event: {}, ErrType: {}, TErrType: {}, ErrName: {},  Time: {}", event.getEventType(), event.getErrorDefinition(), event.getThriftErrorType(), Optional.ofNullable(event.getErrorDefinition()).map(ed -> ed.getErrorName()).orElse(""), event.getTimeStamp());
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
