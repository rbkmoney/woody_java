package com.rbkmoney.woody.api.event;

public class CompositeClientEventListener<E extends ClientEvent> extends CompositeEventListener<E> implements ClientEventListener<E> {

    public CompositeClientEventListener(EventListener<E>... listeners) {
        super(listeners);
    }

}
