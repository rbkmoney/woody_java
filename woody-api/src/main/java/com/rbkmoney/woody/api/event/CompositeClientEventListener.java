package com.rbkmoney.woody.api.event;

/**
 * Created by vpankrashkin on 28.04.16.
 */
public class CompositeClientEventListener<E extends ClientEvent> extends CompositeEventListener<E> implements ClientEventListener<E> {

    public CompositeClientEventListener(EventListener<E>... listeners) {
        super(listeners);
    }

}
