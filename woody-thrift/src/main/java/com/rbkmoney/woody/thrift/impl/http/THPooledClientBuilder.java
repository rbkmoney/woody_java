package com.rbkmoney.woody.thrift.impl.http;

import com.rbkmoney.woody.api.WoodyInstantiationException;
import com.rbkmoney.woody.api.event.ClientEventListener;
import com.rbkmoney.woody.api.generator.IdGenerator;
import com.rbkmoney.woody.api.proxy.InvocationTargetProvider;
import com.rbkmoney.woody.api.proxy.CPool2TargetProvider;
import org.apache.commons.pool2.impl.AbandonedConfig;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.http.client.HttpClient;

import java.net.URI;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by vpankrashkin on 09.06.16.
 *
 * This builder provides the ability to build thread-safe clients around not thread-safe Thrift clients.
 * It uses apache commons-pool2 for internal Thrift client pooling.
 */
public class THPooledClientBuilder extends THClientBuilder {
    private volatile boolean httpClientSet = false;
    private volatile boolean collectInstance = false;
    private volatile GenericObjectPoolConfig poolConfig;
    private volatile AbandonedConfig poolAbandonedConfig;
    private boolean destroyed = false;
    private final ConcurrentLinkedQueue<CPool2TargetProvider> collectedProviders = new ConcurrentLinkedQueue<>();
    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();

    @Override
    public THPooledClientBuilder withHttpClient(HttpClient httpClient) {
        httpClientSet = true;
        return (THPooledClientBuilder) super.withHttpClient(httpClient);
    }

    /**
     * If you're using pooling config which spawns any threads, you need to set {@link THPooledClientBuilder#collectInstance} field to collect all built instances and shutdown them later with {@link THPooledClientBuilder#destroy()} method.
     * @param collectInstance true - if you want to collect all created instances for future destroy, false - otherwise.
     * @return current builder instance.
     * */
    public THPooledClientBuilder withCollectInstance(boolean collectInstance) {
        this.collectInstance = collectInstance;
        return this;
    }

    @Override
    public HttpClient getHttpClient() {
        if (httpClientSet) {
            return super.getHttpClient();
        } else {
            return createHttpClient();
        }

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
     * @param poolConfig pooling config to use instead default one.
     * */
    public THPooledClientBuilder withPoolConfig(GenericObjectPoolConfig poolConfig) {
        this.poolConfig = poolConfig;
        return this;
    }

    /**
     * Use this method if you want to customize abandoned pooling configuration. Built in pool configuration will be used By default.
     * @param poolAbandonedConfig abandoned pooling config to use instead default one.
     * */
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
            CPool2TargetProvider<T> targetProvider = CPool2TargetProvider.newInstance(iface, () -> createProviderClient(iface), poolConfig, poolAbandonedConfig);
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
     * */
    public void destroy() {
        rwLock.writeLock().lock();
        try {
            destroyed = true;
        } finally {
            rwLock.writeLock().unlock();
        }
        for (CPool2TargetProvider provider: collectedProviders) {
            provider.close();
        }
    }


}
