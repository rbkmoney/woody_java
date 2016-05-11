package com.rbkmoney.woody.api;

import com.rbkmoney.woody.api.event.ServiceEventListener;
import com.rbkmoney.woody.api.generator.IdGenerator;

/**
 * Created by vpankrashkin on 22.04.16.
 */
public interface ServiceBuilder<Service> {
    ServiceBuilder withEventListener(ServiceEventListener listener);

    ServiceBuilder withIdGenerator(IdGenerator generator);

    <T> Service build(Class<T> serviceInterface, T serviceHandler);
}
