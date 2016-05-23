package com.rbkmoney.woody.api.event;

/**
 * Created by vpankrashkin on 28.04.16.
 */
public class CompositeServiceEventListener<E extends ServiceEvent> extends CompositeEventListener<E> implements ServiceEventListener<E> {

    public CompositeServiceEventListener(EventListener<E>... listeners) {
        super(listeners);
    }

}
