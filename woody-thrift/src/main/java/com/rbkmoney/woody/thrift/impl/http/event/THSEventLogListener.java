package com.rbkmoney.woody.thrift.impl.http.event;

import com.rbkmoney.woody.api.event.ServiceEventListener;
import com.rbkmoney.woody.api.trace.ContextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;

/**
 * Created by vpankrashkin on 12.05.16.
 */
public class THSEventLogListener implements ServiceEventListener<THServiceEvent> {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public void notifyEvent(THServiceEvent event) {
        try {
            switch (event.getEventType()) {
                case CALL_HANDLER:
                    log.info("SRV: {}, [{}, {}]", event.getEventType(), event.getCallName(), event.getCallType());
                    break;
                case HANDLER_RESULT:
                    log.info("SRV: {}, CRes: {}, HTime: {}ms", event.getEventType(), event.isSuccessfulCall() ? "ok" : "error", (System.currentTimeMillis() - event.getTimeStamp()));
                    break;
                case SERVICE_RECEIVE:
                    HttpServletRequest request = event.getTransportRequest();
                    if (request == null) {
                        log.info("SRV: {} [no transport request]", event.getEventType());
                    } else if (log.isInfoEnabled()) {
                        log.info("SRV: {}, [EP: {}, Url: {}, Src: [{}]:{}, Headers: {}]", event.getEventType(), event.getEndpoint().getStringValue(), buildUrl(request), request.getRemoteAddr(), request.getRemotePort(), buildHeaders(request));
                    }
                    break;
                case SERVICE_RESULT:
                    HttpServletResponse response = event.getTransportResponse();
                    if (response == null) {
                        log.info("SRV: {} [no transport response]", event.getEventType());
                    } else if (log.isInfoEnabled()) {
                        log.info("SRV: {}, CRes: {}, [Status: {}, Headers: {}]", event.getEventType(), event.isSuccessfulCall() ? "ok" : "error", response.getStatus(), buildHeaders(response));
                    }
                    break;
                case ERROR:
                    Throwable error = ContextUtils.getCallError(event.getActiveSpan());
                    if (error == null)
                        error = ContextUtils.getInterceptionError(event.getActiveSpan());
                    log.info("SRV: {}, [ErrDef: {}, TErrType: {}], Time: {}ms", event.getEventType(), event.getErrorDefinition(), event.getThriftErrorType(), (System.currentTimeMillis() - event.getTimeStamp()), error);
                    break;
                default:
                    log.info("SRV Unknown error: {}", event);
                    break;

            }
        } catch (Exception e) {
            log.error("Failed to process event", e);
        }

    }

    private String buildUrl(HttpServletRequest request) {
        StringBuffer requestURL = request.getRequestURL();
        String queryString = request.getQueryString();
        if (queryString == null) {
            return requestURL.toString();
        } else {
            return requestURL.append('?').append(queryString).toString();
        }
    }

    private String buildHeaders(HttpServletRequest httpRequest) {
        StringBuilder sb = new StringBuilder();
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

    private String buildHeaders(HttpServletResponse httpResponse) {
        StringBuilder sb = new StringBuilder();
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
