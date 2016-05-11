package com.rbkmoney.woody.api.proxy;

import java.lang.reflect.Method;

/**
 * Created by vpankrashkin on 22.04.16.
 */
public abstract class InstanceMethodCaller {
    private final Method targetMethod;

    public InstanceMethodCaller(Method targetMethod) {
        this.targetMethod = targetMethod;
    }

    public Method getTargetMethod() {
        return targetMethod;
    }

    public abstract Object call(Object[] args) throws Throwable;

}
