package com.rbkmoney.woody.api.trace;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by vpankrashkin on 21.04.16.
 */
public class ServiceSpan extends ContextSpan {

    private final AtomicInteger counter;

    public ServiceSpan() {
        counter = new AtomicInteger();
    }

    protected ServiceSpan(ServiceSpan serviceSpan) {
        super(serviceSpan);
        this.counter = serviceSpan.counter;
    }

    public ServiceSpan cloneObject() {
        return new ServiceSpan(this);
    }

    public AtomicInteger getCounter() {
        return counter;
    }

    public void reset() {
        super.reset();
        counter.set(0);
    }
}
