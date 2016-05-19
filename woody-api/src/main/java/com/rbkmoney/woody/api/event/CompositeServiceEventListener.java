package com.rbkmoney.woody.api.event;

/**
 * Created by vpankrashkin on 28.04.16.
 */
public class CompositeServiceEventListener<E extends ServiceEvent> extends CompositeEventListener<E> implements ServiceEventListener<E> {
    private final EventListener<E>[] listeners;

    public CompositeServiceEventListener(EventListener<E>... listeners) {
        this.listeners = listeners.clone();
    }

    @Override
    public void notifyEvent(E event) {
        for (int i = 0; i < listeners.length; ++i) {
            listeners[i].notifyEvent(event);
        }
    }
}
