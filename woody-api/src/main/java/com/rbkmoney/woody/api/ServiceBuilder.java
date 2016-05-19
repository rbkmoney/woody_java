package com.rbkmoney.woody.api;

import com.rbkmoney.woody.api.event.ServiceEventListener;

/**
 * Created by vpankrashkin on 22.04.16.
 */
public interface ServiceBuilder<Service> {
    ServiceBuilder withEventListener(ServiceEventListener listener);

    <T> Service build(Class<T> iface, T serviceHandler);
}
