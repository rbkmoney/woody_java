package com.rbkmoney.woody.api.event;

/**
 * Created by vpankrashkin on 25.04.16.
 */
public interface ServiceEventListener extends EventListener<ServiceEvent> {
    void notifyEvent(ServiceEvent event);
}
