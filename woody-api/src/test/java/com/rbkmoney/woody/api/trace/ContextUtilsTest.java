package com.rbkmoney.woody.api.trace;

import org.junit.Assert;
import org.junit.Test;

import java.time.Instant;

public class ContextUtilsTest {

    @Test
    public void getExecutionTimeout() {

        ContextSpan span = new ContextSpan();
        ContextUtils.setDeadline(span, Instant.now());

        int executionTimeout = ContextUtils.getExecutionTimeout(span, 100);

        Assert.assertEquals(0, executionTimeout);
    }

}