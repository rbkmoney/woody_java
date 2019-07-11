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


    @Test
    public void timeTest() {

        Instant instant = Instant.now();

        String expected = instant.toString();

        long epochMilli = instant.toEpochMilli();

        Instant parsedTime = Instant.ofEpochMilli(epochMilli);

        Assert.assertEquals(expected, parsedTime.toString());
    }
}