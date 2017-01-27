package com.rbkmoney.woody.api.flow;

import com.rbkmoney.woody.api.trace.Span;
import com.rbkmoney.woody.api.trace.context.TraceContext;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by vpankrashkin on 11.12.16.
 */
public class TestWFlow {
    @Test
    public void testRunnableServiceFork() {
        assertTrue(TraceContext.getCurrentTraceData().isRoot());
        new WFlow().createServiceFork(() -> {
            assertFalse(TraceContext.getCurrentTraceData().isRoot());
            assertFalse(TraceContext.getCurrentTraceData().isClient());
    }).run();
        assertTrue(TraceContext.getCurrentTraceData().isRoot());
    }

    @Test
    public void testCallableServiceFork() throws Exception {
        assertTrue(TraceContext.getCurrentTraceData().isRoot());
        assertTrue(new WFlow().createServiceFork(() -> {
            assertFalse(TraceContext.getCurrentTraceData().isRoot());
            assertFalse(TraceContext.getCurrentTraceData().isClient());
            return true;
        }).call());
        assertTrue(TraceContext.getCurrentTraceData().isRoot());
    }

    @Test
    public void testGeneratedIds() {
        new WFlow().createServiceFork(() -> {
            Span activeSpan = TraceContext.getCurrentTraceData().getActiveSpan().getSpan();
            assertEquals(activeSpan.getParentId(), TraceContext.NO_PARENT_ID);
            assertNotEquals(activeSpan.getTraceId(), activeSpan.getId());
        }).run();
    }
}
