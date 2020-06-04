package com.rbkmoney.woody.api.proxy;

import java.util.function.Supplier;

public class SpawnTargetProvider<T> implements InvocationTargetProvider<T> {
    private final Class<T> targetType;
    private final Supplier<T> supplier;

    public SpawnTargetProvider(Class<T> targetType, Supplier<T> supplier) {
        this.targetType = targetType;
        this.supplier = supplier;
    }

    @Override
    public T getTarget() {
        return createTarget();
    }

    @Override
    public Class<T> getTargetType() {
        return targetType;
    }

    @Override
    public void releaseTarget(T target) {
    }

    @Override
    public boolean isSingleTarget() {
        return false;
    }

    protected T createTarget() {
        return supplier.get();
    }
}
