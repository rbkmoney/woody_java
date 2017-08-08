package com.rbkmoney.woody.thrift.impl.http.event;

import com.rbkmoney.woody.api.event.ClientEventListener;
import com.rbkmoney.woody.api.trace.ContextUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * Created by vpankrashkin on 12.05.16.
 */
public class THCEventLogListener implements ClientEventListener<THClientEvent> {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public void notifyEvent(THClientEvent event) {
        try {
            switch (event.getEventType()) {
                case CALL_SERVICE:
                    log.info("CLN: {}, [{}, {}]", event.getEventType(), event.getCallName(), event.getCallType());
                    break;
                case CLIENT_SEND:
                    HttpRequestBase request = event.getTransportRequest();
                    if (request == null) {
                        log.info("CLN: {} [no transport request]", event.getEventType());
                    } else if (log.isInfoEnabled()) {
                        log.info("CLN: {}, [EP: {}, Headers: {}]", event.getEventType(), event.getEndpoint().getStringValue(), Arrays.toString(request.getAllHeaders()));
                    }
                    break;
                case CLIENT_RECEIVE:
                    HttpResponse response = event.getTransportResponse();
                    if (response == null) {
                        log.info("CLN: {} [no transport response]", event.getEventType());
                    } else if (log.isInfoEnabled()) {
                        log.info("CLN: {}, CRes: {}, [StLine: {}, Headers: {}]", event.getEventType(), event.isSuccessfulCall() ? "ok" : "error", response.getStatusLine().toString(), Arrays.toString(response.getAllHeaders()));
                    }
                    break;
                case SERVICE_RESULT:
                    log.info("CLN: {}, CRes: {}, Time: {}ms", event.getEventType(), event.isSuccessfulCall() ? "ok" : "error", (System.currentTimeMillis() - event.getTimeStamp()));
                    break;
                case ERROR:
                    Throwable error = ContextUtils.getCallError(event.getActiveSpan());
                    if (error == null)
                        error = ContextUtils.getInterceptionError(event.getActiveSpan());
                    log.warn("CLN: {}, [ErrDef: {}, TErrType: {}], Time: {}ms", event.getEventType(), event.getErrorDefinition(), event.getThriftErrorType(), (System.currentTimeMillis() - event.getTimeStamp()), error);
                    break;
                default:
                    log.info("CLN Unknown error: {}", event);
                    break;
            }
        } catch (Exception e) {
            log.error("Event processing failed", e);
        }
    }
}
