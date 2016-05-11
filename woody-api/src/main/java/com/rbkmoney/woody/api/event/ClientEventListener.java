package com.rbkmoney.woody.api.event;

/**
 * Created by vpankrashkin on 22.04.16.
 */
public interface ClientEventListener extends EventListener<ClientEvent> {
    void notifyEvent(ClientEvent event);

}
