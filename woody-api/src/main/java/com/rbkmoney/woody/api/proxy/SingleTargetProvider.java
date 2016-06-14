package com.rbkmoney.woody.api.proxy;

/**
 * Created by vpankrashkin on 09.06.16.
 */
public class SingleTargetProvider<T> implements InvocationTargetProvider<T> {
    private final Class<T> targetType;
    private final T target;

    public SingleTargetProvider(T target) {
        this((Class<T>) target.getClass(), target);
    }

    public SingleTargetProvider(Class<T> targetType, T target) {
        this.targetType = targetType;
        this.target = target;
    }

    @Override
    public Class<T> getTargetType() {
        return targetType;
    }

    @Override
    public T getTarget() {
        return target;
    }

    @Override
    public boolean isSingleTarget() {
        return true;
    }

    @Override
    public void releaseTarget(T target) {

    }
}
