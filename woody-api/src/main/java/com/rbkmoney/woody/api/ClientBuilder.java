package com.rbkmoney.woody.api;

import com.rbkmoney.woody.api.event.ClientEventListener;
import com.rbkmoney.woody.api.generator.IdGenerator;

import java.net.URI;

/**
 * Created by vpankrashkin on 22.04.16.
 */
public interface ClientBuilder {
    ClientBuilder withAddress(URI address);

    ClientBuilder withEventListener(ClientEventListener listener);

    ClientBuilder withIdGenerator(IdGenerator generator);

    <T> T build(Class<T> iface);
}
