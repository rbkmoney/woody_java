package com.rbkmoney.woody.api.flow.concurrent;

import com.rbkmoney.woody.api.MDCUtils;
import com.rbkmoney.woody.api.trace.Span;
import com.rbkmoney.woody.api.trace.TraceData;
import com.rbkmoney.woody.api.trace.context.TraceContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.concurrent.*;

import static org.junit.Assert.assertEquals;

/**
 * Created by tolkonepiu on 09.06.16.
 */
public class TestMDCInConcurent {

    Logger log = LoggerFactory.getLogger(this.getClass());

    private WExecutorService executorService;

    @Before
    public void before() {
        executorService = new WExecutorService(Executors.newFixedThreadPool(2));

    }

    Runnable runnable = () -> {
        try {
            Span span = TraceContext.getCurrentTraceData().getActiveSpan().getSpan();
            log.info("Runnable {} {} {}", span.getId(), span.getParentId(), span.getTraceId());
            assertEquals(MDC.get(MDCUtils.SPAN_ID), span.getId());
            assertEquals(MDC.get(MDCUtils.TRACE_ID), span.getTraceId());
            assertEquals(MDC.get(MDCUtils.PARENT_ID), span.getParentId());
        } catch (Throwable t) {
            t.printStackTrace();
        }
    };

    Callable callable = () -> {
        try {
            Span span = TraceContext.getCurrentTraceData().getActiveSpan().getSpan();
            log.info("Callable {} {} {}", span.getId(), span.getParentId(), span.getTraceId());
            assertEquals(MDC.get(MDCUtils.SPAN_ID), span.getId());
            assertEquals(MDC.get(MDCUtils.TRACE_ID), span.getTraceId());
            assertEquals(MDC.get(MDCUtils.PARENT_ID), span.getParentId());
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return null;
    };


    @Test
    public void testMDCCallable() throws ExecutionException, InterruptedException {
        TraceData traceData = TraceContext.getCurrentTraceData();
        traceData.getActiveSpan().getSpan().setId("span1");
        traceData.getActiveSpan().getSpan().setTraceId("trace1");
        traceData.getActiveSpan().getSpan().setParentId("parent1");

        Future future1 = executorService.submit(callable);

        traceData.getActiveSpan().getSpan().setId("span2");
        traceData.getActiveSpan().getSpan().setTraceId("trace2");
        traceData.getActiveSpan().getSpan().setParentId("parent2");

        Future future2 = executorService.submit(callable);

        future1.get();
        future2.get();

        Assert.assertSame(traceData, TraceContext.getCurrentTraceData());
    }

    @Test
    public void testMDCRunnable() throws ExecutionException, InterruptedException {
        TraceData traceData = TraceContext.getCurrentTraceData();
        traceData.getActiveSpan().getSpan().setId("span1");
        traceData.getActiveSpan().getSpan().setTraceId("trace1");
        traceData.getActiveSpan().getSpan().setParentId("parent1");

        Future future1 = executorService.submit(runnable);

        traceData.getActiveSpan().getSpan().setId("span2");
        traceData.getActiveSpan().getSpan().setTraceId("trace2");
        traceData.getActiveSpan().getSpan().setParentId("parent2");

        Future future2 = executorService.submit(runnable);

        future1.get();
        future2.get();

        Assert.assertSame(traceData, TraceContext.getCurrentTraceData());
    }

}
