package com.rbkmoney.woody.api.flow.concurrent;

import com.rbkmoney.woody.api.trace.TraceData;
import com.rbkmoney.woody.api.trace.context.TraceContext;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by vpankrashkin on 19.05.16.
 */
public class TestWExecutorService {
    private WExecutorService executorService;

    @Before
    public void before() {
        executorService = new WExecutorService(Executors.newSingleThreadExecutor());

    }

    @Test
    public void testRunnable() throws ExecutionException, InterruptedException {
        AtomicBoolean hasErrors = new AtomicBoolean();
        TraceData traceData = TraceContext.getCurrentTraceData();
        traceData.getActiveSpan().getSpan().setId("testID");
        traceData.getActiveSpan().getMetadata().putValue(Boolean.TRUE.toString(), new Object());
        Future future = executorService.submit(() -> {
            try {
                TraceData cData = TraceContext.getCurrentTraceData();
                Assert.assertNotSame(traceData, cData);
                Assert.assertEquals(traceData.getActiveSpan().getSpan().getId(), cData.getActiveSpan().getSpan().getId());
                Assert.assertSame(traceData.getActiveSpan().getMetadata().<Object>getValue(Boolean.TRUE.toString()), cData.getActiveSpan().getMetadata().getValue(Boolean.TRUE.toString()));
                Assert.assertNotSame(traceData, cData);
            } catch (Throwable t) {
                hasErrors.set(true);
                t.printStackTrace();
            }
        });
        future.get();
        Assert.assertSame(traceData, TraceContext.getCurrentTraceData());
        Assert.assertFalse(hasErrors.get());

    }

    @Test
    public void testCallable() throws ExecutionException, InterruptedException {
        AtomicBoolean hasErrors = new AtomicBoolean();
        TraceData traceData = TraceContext.getCurrentTraceData();
        traceData.getActiveSpan().getSpan().setId("testID");
        traceData.getActiveSpan().getMetadata().putValue(Boolean.TRUE.toString(), new Object());
        Future future = executorService.submit(() -> {
            try {
                TraceData cData = TraceContext.getCurrentTraceData();
                Assert.assertNotSame(traceData, cData);
                Assert.assertEquals(traceData.getActiveSpan().getSpan().getId(), cData.getActiveSpan().getSpan().getId());
                Assert.assertSame(traceData.getActiveSpan().getMetadata().<Object>getValue(Boolean.TRUE.toString()), cData.getActiveSpan().getMetadata().getValue(Boolean.TRUE.toString()));
                Assert.assertNotSame(traceData, cData);
            } catch (Throwable t) {
                hasErrors.set(true);
                t.printStackTrace();
            }
            return null;
        });
        future.get();
        Assert.assertSame(traceData, TraceContext.getCurrentTraceData());
        Assert.assertFalse(hasErrors.get());

    }

    @After
    public void after() {
        executorService.shutdownNow();
    }

}
