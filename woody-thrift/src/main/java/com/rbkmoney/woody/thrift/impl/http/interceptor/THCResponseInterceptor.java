package com.rbkmoney.woody.thrift.impl.http.interceptor;

import com.rbkmoney.woody.api.interceptor.ResponseInterceptor;
import com.rbkmoney.woody.api.trace.ClientSpan;
import com.rbkmoney.woody.api.trace.ContextUtils;
import com.rbkmoney.woody.api.trace.TraceData;
import com.rbkmoney.woody.thrift.impl.http.THMetadataProperties;
import org.apache.http.HttpResponse;

import java.io.IOException;
import java.net.HttpURLConnection;

/**
 * Created by vpankrashkin on 29.04.16.
 */
public class THCResponseInterceptor implements ResponseInterceptor {
    @Override
    public boolean interceptResponse(TraceData traceData, Object providerContext, Object... contextParams) {
        if (providerContext instanceof HttpResponse) {
            return interceptResponseBase(traceData.getClientSpan(), (HttpResponse) providerContext);
        } else if (providerContext instanceof HttpURLConnection) {
            return interceptUrlConnection(traceData.getClientSpan(), (HttpURLConnection) providerContext);
        }
        return interceptError(traceData.getClientSpan(), "Unknown type:" + providerContext.getClass(), null);
    }

    private boolean interceptUrlConnection(ClientSpan clientSpan, HttpURLConnection connection) {
        try {
            clientSpan.getMetadata().putValue(THMetadataProperties.TH_RESPONSE_STATUS, connection.getResponseCode());
            clientSpan.getMetadata().putValue(THMetadataProperties.TH_RESPONSE_MESSAGE, connection.getResponseMessage());
            return true;
        } catch (IOException e) {
            return interceptError(clientSpan, "Failed to get response data", e);
        }
    }

    protected boolean interceptResponseBase(ClientSpan clientSpan, HttpResponse response) {
        clientSpan.getMetadata().putValue(THMetadataProperties.TH_RESPONSE_STATUS, response.getStatusLine().getStatusCode());
        clientSpan.getMetadata().putValue(THMetadataProperties.TH_RESPONSE_MESSAGE, response.getStatusLine().getReasonPhrase());
        return true;
    }

    private boolean interceptError(ClientSpan clientSpan, String message, Throwable cause) {
        ContextUtils.setInterceptionError(clientSpan, cause);
        return false;
    }

}
