package com.rbkmoney.woody.api.trace;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by vpankrashkin on 21.04.16.
 */
public class ServiceSpan extends ContextSpan {

    private final AtomicInteger counter = new AtomicInteger();

    public AtomicInteger getCounter() {
        return counter;
    }

    public void reset() {
        super.reset();
        counter.set(0);
    }
}
