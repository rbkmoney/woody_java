package com.rbkmoney.woody.thrift.impl.http.event;

import com.rbkmoney.woody.api.event.ServiceEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;

public class HttpServiceEventLogListener implements ServiceEventListener<THServiceEvent> {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public void notifyEvent(THServiceEvent event) {
        try {
            switch (event.getEventType()) {
                case SERVICE_RECEIVE:
                    HttpServletRequest request = event.getTransportRequest();
                    if (request != null) {
                        log.info("SERVER Event: {}, {}", event.getEventType(), buildRequestLog(request));
                    }
                    break;
                case SERVICE_RESULT:
                    HttpServletResponse response = event.getTransportResponse();
                    if (response != null) {
                        log.info("SERVER Event: {}, {}", event.getEventType(), buildResponseLog(response));
                    }
                    break;
            }
        } catch (Exception e) {
            log.error("Failed to process event", e);
        }

    }

    private String buildRequestLog(HttpServletRequest httpRequest) {
        StringBuilder sb = new StringBuilder();
        sb.append("HttpRequest:")
                .append(httpRequest.getMethod()).append(" ")
                .append(httpRequest.getProtocol())
                .append(", RemoteHost: ").append(httpRequest.getRemoteHost())
                .append(", Headers:");
        Enumeration<String> headers = httpRequest.getHeaderNames();
        sb.append('[');
        for (String headerName; headers.hasMoreElements(); ) {
            headerName = headers.nextElement();
            Enumeration<String> vals = httpRequest.getHeaders(headerName);
            for (String val; vals.hasMoreElements(); ) {
                val = vals.nextElement();
                sb.append(headerName).append(": ").append(val);
                if (vals.hasMoreElements())
                    sb.append(", ");
            }
            if (headers.hasMoreElements()) {
                sb.append(", ");
            }
        }
        sb.append(']');

        return sb.toString();

    }

    private String buildResponseLog(HttpServletResponse httpResponse) {
        StringBuilder sb = new StringBuilder();
        sb.append("HttpResponse:")
                .append(httpResponse.getStatus())
                .append(", Headers:");
        Collection<String> headers = httpResponse.getHeaderNames();
        sb.append('[');
        for (Iterator<String> it = headers.iterator(); it.hasNext(); ) {
            String header = it.next();
            sb.append(header).append(": ").append(httpResponse.getHeader(header));
            if (it.hasNext())
                sb.append(", ");
        }
        sb.append(']');
        return sb.toString();
    }
}
