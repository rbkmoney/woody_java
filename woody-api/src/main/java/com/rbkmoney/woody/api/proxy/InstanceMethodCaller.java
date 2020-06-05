package com.rbkmoney.woody.api.proxy;

import java.lang.reflect.Method;

public abstract class InstanceMethodCaller {
    private final Method targetMethod;

    public InstanceMethodCaller(Method targetMethod) {
        this.targetMethod = targetMethod;
    }

    public Method getTargetMethod() {
        return targetMethod;
    }

    public abstract Object call(Object source, Object[] args) throws Throwable;

}
