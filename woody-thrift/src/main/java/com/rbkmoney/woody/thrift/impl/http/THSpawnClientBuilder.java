package com.rbkmoney.woody.thrift.impl.http;

import com.rbkmoney.woody.api.WoodyInstantiationException;
import com.rbkmoney.woody.api.event.ClientEventListener;
import com.rbkmoney.woody.api.flow.error.WErrorMapper;
import com.rbkmoney.woody.api.generator.IdGenerator;
import com.rbkmoney.woody.api.proxy.InvocationTargetProvider;
import com.rbkmoney.woody.api.proxy.SpawnTargetProvider;
import com.rbkmoney.woody.api.trace.context.metadata.MetadataExtensionKit;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;

import java.net.URI;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * Created by vpankrashkin on 09.06.16.
 * <p>
 * This builder provides the ability to build thread-safe clients around not thread-safe Thrift clients.
 * It creates new Thrift client instance for every call which is dropped after call finishes.
 */
public class THSpawnClientBuilder extends THClientBuilder {

    public THSpawnClientBuilder() {
    }

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
    public THSpawnClientBuilder withNetworkTimeout(int timeout) {
        return (THSpawnClientBuilder) super.withNetworkTimeout(timeout);
    }

    @Override
    public THSpawnClientBuilder withLogEnabled(boolean enabled) {
        return (THSpawnClientBuilder) super.withLogEnabled(enabled);
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

    @Override
    protected HttpClient createHttpClient() {
        return HttpClients.createMinimal(new BasicHttpClientConnectionManager());
    }

    private <T> InvocationTargetProvider<T> createTargetProvider(Class<T> iface) {
        SpawnTargetProvider<T> targetProvider = new THSpawnTargetProvider<>(
                iface,
                () -> createProviderClient(iface),
                this::destroyProviderClient,
                isCustomHttpClient());
        return targetProvider;
    }

    private static class THSpawnTargetProvider<T> extends SpawnTargetProvider<T> {
        private final boolean customClient;
        private final BiConsumer<Object, Boolean> releaseConsumer;


        public THSpawnTargetProvider(Class<T> targetType, Supplier<T> supplier, BiConsumer<Object, Boolean> releaseConsumer, boolean customClient) {
            super(targetType, supplier);
            this.customClient = customClient;
            this.releaseConsumer = releaseConsumer;
        }

        @Override
        public void releaseTarget(T target) {
            releaseConsumer.accept(target, customClient);
            super.releaseTarget(target);
        }
    }

}
