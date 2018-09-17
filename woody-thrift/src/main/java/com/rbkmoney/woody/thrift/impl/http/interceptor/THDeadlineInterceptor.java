package com.rbkmoney.woody.thrift.impl.http.interceptor;

import com.rbkmoney.woody.api.flow.error.WUnavailableResultException;
import com.rbkmoney.woody.api.interceptor.EmptyCommonInterceptor;
import com.rbkmoney.woody.api.trace.ContextSpan;
import com.rbkmoney.woody.api.trace.ContextUtils;
import com.rbkmoney.woody.api.trace.TraceData;
import com.rbkmoney.woody.thrift.impl.http.interceptor.ext.THCExtensionContext;
import com.rbkmoney.woody.thrift.impl.http.interceptor.ext.THSExtensionContext;
import com.rbkmoney.woody.thrift.impl.http.transport.THttpHeader;
import com.rbkmoney.woody.thrift.impl.http.transport.TTransportErrorType;

import javax.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.time.format.DateTimeParseException;

public class THDeadlineInterceptor extends EmptyCommonInterceptor {

    public static THDeadlineInterceptor forClient(int networkTimeout) {
        return new THDeadlineInterceptor(true, networkTimeout);
    }

    public static THDeadlineInterceptor forService() {
        return new THDeadlineInterceptor(false);
    }

    private final boolean isClient;

    private final int networkTimeout;

    private THDeadlineInterceptor(boolean isClient) {
        this.isClient = isClient;
        this.networkTimeout = -1;
    }

    private THDeadlineInterceptor(boolean isClient, Integer networkTimeout) {
        this.isClient = isClient;
        this.networkTimeout = networkTimeout;
    }

    @Override
    public boolean interceptRequest(TraceData traceData, Object providerContext, Object... contextParams) {
        if (isClient) {
            interceptRequestForClient(traceData, providerContext, contextParams);
        } else {
            interceptRequestForService(traceData, providerContext, contextParams);
        }

        return true;
    }

    private void interceptRequestForService(TraceData traceData, Object providerContext, Object[] contextParams) {
        THSExtensionContext extensionContext = new THSExtensionContext(traceData, providerContext, contextParams);
        HttpServletRequest request = extensionContext.getProviderRequest();
        ContextSpan contextSpan = extensionContext.getTraceData().getActiveSpan();
        String deadlineHeaderValue = request.getHeader(THttpHeader.DEADLINE.getKey()) != null ? request.getHeader(THttpHeader.DEADLINE.getKey()) : request.getHeader(THttpHeader.DEADLINE.getOldKey());

        if (deadlineHeaderValue != null && !deadlineHeaderValue.isEmpty()) {
            try {
                Instant deadline = Instant.parse(deadlineHeaderValue);
                validateDeadline(deadline);
                ContextUtils.setDeadline(contextSpan, deadline);
            } catch (DateTimeParseException ex) {
                throw new THRequestInterceptionException(TTransportErrorType.BAD_HEADER, THttpHeader.DEADLINE.getKey(), ex);
            }
        }
    }

    private void interceptRequestForClient(TraceData traceData, Object providerContext, Object... contextParams) {
        THCExtensionContext extensionContext = new THCExtensionContext(traceData, providerContext, contextParams);
        Instant deadline = ContextUtils.getDeadline(traceData.getActiveSpan());
        if (deadline != null) {
            validateDeadline(deadline);
            extensionContext.setRequestHeader(THttpHeader.DEADLINE.getKey(), deadline.toString());
            //old header
            extensionContext.setRequestHeader(THttpHeader.DEADLINE.getOldKey(), deadline.toString());
        } else {
            if (networkTimeout > 0) {
                deadline = Instant.now().plusMillis(networkTimeout);
                ContextUtils.setDeadline(traceData.getClientSpan(), deadline);
                extensionContext.setRequestHeader(THttpHeader.DEADLINE.getKey(), deadline.toString());
                //old header
                extensionContext.setRequestHeader(THttpHeader.DEADLINE.getOldKey(), deadline.toString());
            }
        }
    }

    @Override
    public boolean interceptResponse(TraceData traceData, Object providerContext, Object... contextParams) {
        if (!isClient) {
            THSExtensionContext extensionContext = new THSExtensionContext(traceData, providerContext, contextParams);
            Instant deadline = ContextUtils.getDeadline(traceData.getServiceSpan());
            if (deadline != null) {
                extensionContext.setResponseHeader(THttpHeader.DEADLINE.getKey(), deadline.toString());
                //old header
                extensionContext.setResponseHeader(THttpHeader.DEADLINE.getOldKey(), deadline.toString());
            }
        }
        return true;
    }

    private void validateDeadline(Instant deadline) {
        if (deadline.isBefore(Instant.now())) {
            throw new WUnavailableResultException("deadline reached");
        }
    }

}
