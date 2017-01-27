package com.rbkmoney.woody.thrift.impl.http;

import com.rbkmoney.woody.api.WoodyInstantiationException;
import com.rbkmoney.woody.api.event.ClientEventListener;
import com.rbkmoney.woody.api.flow.error.WErrorMapper;
import com.rbkmoney.woody.api.generator.IdGenerator;
import com.rbkmoney.woody.api.proxy.InvocationTargetProvider;
import com.rbkmoney.woody.api.proxy.SpawnTargetProvider;
import com.rbkmoney.woody.api.trace.context.metadata.MetadataExtensionKit;
import org.apache.http.client.HttpClient;

import java.net.URI;
import java.util.List;

/**
 * Created by vpankrashkin on 09.06.16.
 *
 * This builder provides the ability to build thread-safe clients around not thread-safe Thrift clients.
 * It creates new Thrift client instance for every call which is dropped after call finishes.
 */
public class THSpawnClientBuilder extends THClientBuilder {

    @Override
    public THSpawnClientBuilder withErrorMapper(WErrorMapper errorMapper) {
        return (THSpawnClientBuilder) super.withErrorMapper(errorMapper);
    }

    @Override
    public THSpawnClientBuilder withHttpClient(HttpClient httpClient) {
        return (THSpawnClientBuilder) super.withHttpClient(httpClient);
    }

    @Override
    public THSpawnClientBuilder withMetaExtensions(List<MetadataExtensionKit> extensionKits) {
        return (THSpawnClientBuilder) super.withMetaExtensions(extensionKits);
    }

    @Override
    public THSpawnClientBuilder withAddress(URI address) {
        return (THSpawnClientBuilder) super.withAddress(address);
    }

    @Override
    public THSpawnClientBuilder withEventListener(ClientEventListener listener) {
        return (THSpawnClientBuilder) super.withEventListener(listener);
    }

    @Override
    public THSpawnClientBuilder withIdGenerator(IdGenerator generator) {
        return (THSpawnClientBuilder) super.withIdGenerator(generator);
    }

    @Override
    public <T> T build(Class<T> iface) throws WoodyInstantiationException {
        try {
            return build(iface, createTargetProvider(iface));
        } catch (WoodyInstantiationException e) {
            throw e;
        } catch (Exception e) {
            throw new WoodyInstantiationException(e);
        }
    }

    private <T> InvocationTargetProvider<T> createTargetProvider(Class<T> iface) {
            SpawnTargetProvider<T> targetProvider = new SpawnTargetProvider<>(iface, () -> createProviderClient(iface));
            return targetProvider;
    }

}
