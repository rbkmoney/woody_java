package com.rbkmoney.woody.thrift.impl.http;

import com.rbkmoney.woody.api.WoodyInstantiationException;
import com.rbkmoney.woody.api.event.ClientEventListener;
import com.rbkmoney.woody.api.flow.error.WErrorMapper;
import com.rbkmoney.woody.api.generator.IdGenerator;
import com.rbkmoney.woody.api.proxy.CPool2TargetProvider;
import com.rbkmoney.woody.api.proxy.InvocationTargetProvider;
import com.rbkmoney.woody.api.trace.context.metadata.MetadataExtensionKit;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.AbandonedConfig;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.http.client.HttpClient;

import java.net.URI;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * Created by vpankrashkin on 09.06.16.
 * <p>
 * This builder provides the ability to build thread-safe clients around not thread-safe Thrift clients.
 * It uses apache commons-pool2 for internal Thrift client pooling.
 */
public class THPooledClientBuilder extends THClientBuilder {
    private volatile boolean collectInstance = false;
    private volatile GenericObjectPoolConfig poolConfig;
    private volatile AbandonedConfig poolAbandonedConfig;
    private boolean destroyed = false;
    private final ConcurrentLinkedQueue<CPool2TargetProvider> collectedProviders = new ConcurrentLinkedQueue<>();
    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();


    @Override
    public THPooledClientBuilder withErrorMapper(WErrorMapper errorMapper) {
        return (THPooledClientBuilder) super.withErrorMapper(errorMapper);
    }

    @Override
    public THPooledClientBuilder withHttpClient(HttpClient httpClient) {
        return (THPooledClientBuilder) super.withHttpClient(httpClient);
    }

    @Override
    public THPooledClientBuilder withMetaExtensions(List<MetadataExtensionKit> extensionKits) {
        return (THPooledClientBuilder) super.withMetaExtensions(extensionKits);
    }

    /**
     * If you're using pooling config which spawns any threads, you need to set {@link THPooledClientBuilder#collectInstance} field to collect all built instances and shutdown them later with {@link THPooledClientBuilder#destroy()} method.
     *
     * @param collectInstance true - if you want to collect all created instances for future destroy, false - otherwise.
     * @return current builder instance.
     */
    public THPooledClientBuilder withCollectInstance(boolean collectInstance) {
        this.collectInstance = collectInstance;
        return this;
    }

    @Override
    public THPooledClientBuilder withAddress(URI address) {
        return (THPooledClientBuilder) super.withAddress(address);
    }

    @Override
    public THPooledClientBuilder withEventListener(ClientEventListener listener) {
        return (THPooledClientBuilder) super.withEventListener(listener);
    }

    @Override
    public THPooledClientBuilder withIdGenerator(IdGenerator generator) {
        return (THPooledClientBuilder) super.withIdGenerator(generator);
    }

    /**
     * Use this method if you want to customize pooling configuration. Built in pool configuration will be used By default.
     *
     * @param poolConfig pooling config to use instead default one.
     */
    public THPooledClientBuilder withPoolConfig(GenericObjectPoolConfig poolConfig) {
        this.poolConfig = poolConfig;
        return this;
    }

    /**
     * Use this method if you want to customize abandoned pooling configuration. Built in pool configuration will be used By default.
     *
     * @param poolAbandonedConfig abandoned pooling config to use instead default one.
     */
    public THPooledClientBuilder withPoolAbandonedConfig(AbandonedConfig poolAbandonedConfig) {
        this.poolAbandonedConfig = poolAbandonedConfig;
        return this;
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
        rwLock.readLock().lock();
        try {
            if (destroyed) {
                throw new IllegalStateException("Builder is already destroyed");
            }
            CPool2TargetProvider<T> targetProvider = CPool2TargetProvider.newInstance(
                    iface,
                    () -> new THTargetObjectFactory<>(
                            () -> createProviderClient(iface),
                            this::destroyProviderClient,
                            isCustomHttpClient()),
                    poolConfig, poolAbandonedConfig);
            if (collectInstance) {
                collectedProviders.add(targetProvider);
            }
            return targetProvider;
        } finally {
            rwLock.readLock().unlock();
        }
    }

    /**
     * If eviction policy was set in referred pooling config, we need to control pool eviction threads lifecycle for all built instances. This method initiates shutdown for all collected pools.
     * {@link THPooledClientBuilder#collectInstance} flag must be set to make it work properly.
     */
    public void destroy() {
        rwLock.writeLock().lock();
        try {
            destroyed = true;
        } finally {
            rwLock.writeLock().unlock();
        }
        for (CPool2TargetProvider provider : collectedProviders) {
            provider.close();
        }
    }

    private static class THTargetObjectFactory<T> extends CPool2TargetProvider.TargetObjectFactory<T> {
        private final boolean customClient;
        private final BiConsumer<Object, Boolean> destroyTargetConsumer;

        public THTargetObjectFactory(Supplier<T> targetSupplier, BiConsumer<Object, Boolean> destroyTargetConsumer, boolean customClient) {
            super(targetSupplier);
            this.customClient = customClient;
            this.destroyTargetConsumer = destroyTargetConsumer;
        }

        @Override
        public void destroyObject(PooledObject<T> p) throws Exception {
            destroyTargetConsumer.accept(p.getObject(), customClient);
            super.destroyObject(p);
        }
    }


}
