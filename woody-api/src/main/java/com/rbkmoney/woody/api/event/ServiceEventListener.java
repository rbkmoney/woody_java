package com.rbkmoney.woody.api.event;

/**
 * Created by vpankrashkin on 25.04.16.
 */
public interface ServiceEventListener<E extends ServiceEvent> extends EventListener<E> {
    void notifyEvent(E event);
}
