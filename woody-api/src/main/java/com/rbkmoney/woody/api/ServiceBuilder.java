package com.rbkmoney.woody.api;

import com.rbkmoney.woody.api.generator.IdGenerator;

import java.beans.EventHandler;

/**
 * Created by vpankrashkin on 22.04.16.
 */
public interface ServiceBuilder {
    ServiceBuilder withEventHandler(EventHandler handler);

    ServiceBuilder withIdGenerator(IdGenerator generator);

    <T> T build(Class<T> serviceInterface);

    <T> T build(Class<T> serviceInterface, ServiceConfigurator configurator);
}
