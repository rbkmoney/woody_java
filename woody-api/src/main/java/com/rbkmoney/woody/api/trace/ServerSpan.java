package com.rbkmoney.woody.api.trace;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by vpankrashkin on 21.04.16.
 */
public class ServerSpan extends AbstractSpan {
    private Endpoint endpoint;
    private final AtomicInteger counter = new AtomicInteger();

    public Endpoint getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(Endpoint endpoint) {
        this.endpoint = endpoint;
    }

    public AtomicInteger getCounter() {
        return counter;
    }

    public void reset() {
        super.reset();
        endpoint = null;
        counter.set(0);
    }
}
