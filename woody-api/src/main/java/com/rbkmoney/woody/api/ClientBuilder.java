package com.rbkmoney.woody.api;

import com.rbkmoney.woody.api.event.ClientEventListener;
import com.rbkmoney.woody.api.generator.IdGenerator;

import java.net.URI;

public interface ClientBuilder {
    ClientBuilder withAddress(URI address);

    ClientBuilder withEventListener(ClientEventListener listener);

    ClientBuilder withIdGenerator(IdGenerator generator);

    ClientBuilder withNetworkTimeout(int timeout);

    URI getAddress();

    ClientEventListener getEventListener();

    IdGenerator getIdGenerator();

    int getNetworkTimeout();

    <T> T build(Class<T> iface);
}
