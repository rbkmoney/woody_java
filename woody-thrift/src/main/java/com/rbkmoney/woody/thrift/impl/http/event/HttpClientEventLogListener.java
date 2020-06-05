package com.rbkmoney.woody.thrift.impl.http.event;

import com.rbkmoney.woody.api.event.ClientEventListener;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class HttpClientEventLogListener implements ClientEventListener<THClientEvent> {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public void notifyEvent(THClientEvent event) {
        try {

            switch (event.getEventType()) {
                case CLIENT_SEND:
                    HttpRequestBase httpRequest = event.getTransportRequest();
                    if (httpRequest != null) {
                        log.info("CLIENT Event: {}, {}", event.getEventType(), buildRequestLog(httpRequest));
                    }
                    break;
                case CLIENT_RECEIVE:
                    HttpResponse httpResponse = event.getTransportResponse();
                    if (httpResponse != null) {
                        log.info("CLIENT Event: {}, {}", event.getEventType(), buildResponseLog(httpResponse));
                    }
                    break;
            }
        } catch (Exception e) {
            log.error("Event processing failed", e);
        }
    }

    private String buildRequestLog(HttpRequestBase requestBase) {
        StringBuilder sb = new StringBuilder();
        sb.append("HttpRequest:")
                .append(requestBase.toString()).append(", Headers:").append(Arrays.toString(requestBase.getAllHeaders()));

        return sb.toString();

    }

    private String buildResponseLog(HttpResponse httpResponse) {
        StringBuilder sb = new StringBuilder();
        sb.append("HttpResponse:")
                .append(httpResponse.getStatusLine().toString()).append(", Headers:").append(Arrays.toString(httpResponse.getAllHeaders()));

        return sb.toString();
    }
}
