package com.rbkmoney.woody.api.event;

/**
 * Created by vpankrashkin on 28.04.16.
 */
public class CompositeClientEventListener<E extends ClientEvent> extends CompositeEventListener<E> implements ClientEventListener<E> {
    private final EventListener<E>[] listeners;

    public CompositeClientEventListener(EventListener<E>... listeners) {
        this.listeners = listeners.clone();
    }

    @Override
    public void notifyEvent(E event) {
        for (int i = 0; i < listeners.length; ++i) {
            listeners[i].notifyEvent(event);
        }
    }
}
