package com.rbkmoney.woody.api.proxy.tracer;

import com.rbkmoney.woody.api.MDCUtils;
import com.rbkmoney.woody.api.flow.error.WUnavailableResultException;
import com.rbkmoney.woody.api.proxy.InstanceMethodCaller;
import com.rbkmoney.woody.api.trace.ContextSpan;
import com.rbkmoney.woody.api.trace.ContextUtils;
import com.rbkmoney.woody.api.trace.TraceData;
import com.rbkmoney.woody.api.trace.context.TraceContext;

import java.time.Instant;

public class DeadlineTracer extends EmptyTracer {

    public static DeadlineTracer forClient(int networkTimeout) {
        return new DeadlineTracer(true, networkTimeout);
    }

    public static DeadlineTracer forService() {
        return new DeadlineTracer(false);
    }

    private final boolean isClient;

    private final int networkTimeout;

    private DeadlineTracer(boolean isClient) {
        this.isClient = isClient;
        this.networkTimeout = -1;
    }

    private DeadlineTracer(boolean isClient, Integer networkTimeout) {
        this.isClient = isClient;
        this.networkTimeout = networkTimeout;
    }

    @Override
    public void beforeCall(Object[] args, InstanceMethodCaller caller) throws Exception {
        ContextSpan contextSpan = getContextSpan();
        Instant deadline = ContextUtils.getDeadline(contextSpan);
        if (deadline != null) {
            validateDeadline(deadline);
        } else {
            if (isClient && networkTimeout > 0) {
                deadline = Instant.now().plusMillis(networkTimeout);
                ContextUtils.setDeadline(contextSpan, deadline);
                MDCUtils.putDeadline(deadline);
            }
        }
    }

    @Override
    public void afterCall(Object[] args, InstanceMethodCaller caller, Object result) throws Exception {
        ContextSpan contextSpan = getContextSpan();
        if (ContextUtils.getDeadline(contextSpan) == null) {
            MDCUtils.removeDeadline();
        }
    }

    private ContextSpan getContextSpan() {
        TraceData currentTraceData = TraceContext.getCurrentTraceData();
        return isClient ? currentTraceData.getClientSpan() : currentTraceData.getServiceSpan();
    }

    private void validateDeadline(Instant deadline) {
        if (deadline.isBefore(Instant.now())) {
            throw new WUnavailableResultException("deadline reached");
        }
    }

}
