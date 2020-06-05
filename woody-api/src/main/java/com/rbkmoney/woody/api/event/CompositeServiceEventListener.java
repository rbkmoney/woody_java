package com.rbkmoney.woody.api.event;

public class CompositeServiceEventListener<E extends ServiceEvent> extends CompositeEventListener<E> implements ServiceEventListener<E> {

    public CompositeServiceEventListener(EventListener<E>... listeners) {
        super(listeners);
    }

}
