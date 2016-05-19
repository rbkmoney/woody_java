package com.rbkmoney.woody.api.event;

/**
 * Created by vpankrashkin on 22.04.16.
 */
public interface ClientEventListener<E extends ClientEvent> extends EventListener<E> {
    void notifyEvent(E event);

}
