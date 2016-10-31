package com.rbkmoney.woody.api;

import com.rbkmoney.woody.api.event.ServiceEventListener;

/**
 * Created by vpankrashkin on 22.04.16.
 */
public interface ServiceBuilder<Srv> {
    ServiceBuilder withEventListener(ServiceEventListener listener);

    ServiceEventListener getEventListener();

    <T> Srv build(Class<T> iface, T serviceHandler);
}
