package com.rbkmoney.woody.api.event;

public interface ServiceEventListener<E extends ServiceEvent> extends EventListener<E> {
    void notifyEvent(E event);
}
