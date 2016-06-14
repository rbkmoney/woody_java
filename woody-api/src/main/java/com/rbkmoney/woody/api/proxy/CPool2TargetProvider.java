package com.rbkmoney.woody.api.proxy;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.AbandonedConfig;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import java.util.function.Supplier;

/**
 * Created by vpankrashkin on 09.06.16.
 */
public class CPool2TargetProvider<T> implements InvocationTargetProvider<T> {
    private final ObjectPool<T> pool;
    private final Class<T> targetType;
    private final Supplier<T> supplier;

    public static <T> CPool2TargetProvider<T> newInstance(Class<T> targetType, Supplier<T> supplier, GenericObjectPoolConfig config, AbandonedConfig abandonedConfig) {
        if (config == null) {
            return new CPool2TargetProvider<>(targetType, supplier);
        } else if (abandonedConfig == null) {
            return new CPool2TargetProvider<>(targetType, supplier, config);
        } else {
            return new CPool2TargetProvider<>(targetType, supplier, config, abandonedConfig);
        }
    }

    public CPool2TargetProvider(Class<T> targetType, Supplier<T> supplier) {
        this.supplier = supplier;
        this.targetType = targetType;
        this.pool = new GenericObjectPool<>(new TargetObjectFactory<>(this::createTarget));
    }

    public CPool2TargetProvider(Class<T> targetType, Supplier<T> supplier, GenericObjectPoolConfig config) {
        this.supplier = supplier;
        this.targetType = targetType;
        this.pool = new GenericObjectPool<>(new TargetObjectFactory<>(this::createTarget), config);
    }

    public CPool2TargetProvider(Class<T> targetType, Supplier<T> supplier, GenericObjectPoolConfig config, AbandonedConfig abandonedConfig) {
        this.supplier = supplier;
        this.targetType = targetType;
        this.pool = new GenericObjectPool<>(new TargetObjectFactory<>(this::createTarget), config, abandonedConfig);
    }

    @Override
    public T getTarget() throws IllegalStateException {
        try {
            return pool.borrowObject();
        } catch (Exception e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new IllegalStateException("Failed to borrow object", e);
        }
    }

    @Override
    public Class<T> getTargetType() {
        return targetType;
    }

    @Override
    public void releaseTarget(T target) throws IllegalStateException {
        try {
            pool.returnObject(target);
        } catch (Exception e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new IllegalStateException("Failed to release object", e);
        }
    }

    public void close() {
        pool.close();
    }

    @Override
    public boolean isSingleTarget() {
        return false;
    }

    protected T createTarget() {
        return supplier.get();
    }

    private static class TargetObjectFactory<T> extends BasePooledObjectFactory<T> {
        private final Supplier<T> targetSupplier;

        public TargetObjectFactory(Supplier<T> targetSupplier) {
            this.targetSupplier = targetSupplier;
        }

        @Override
        public T create() {
            return targetSupplier.get();
        }

        @Override
        public PooledObject<T> wrap(T target) {
            return new DefaultPooledObject<>(target);
        }
    }
}
