package com.rbkmoney.woody.api.event;

/**
 * Created by vpankrashkin on 25.04.16.
 */
public interface EventListener<E extends Event> {
    void notifyEvent(E event);
}
