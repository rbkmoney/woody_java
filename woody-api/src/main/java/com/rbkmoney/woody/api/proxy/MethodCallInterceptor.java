package com.rbkmoney.woody.api.proxy;

@FunctionalInterface
public interface MethodCallInterceptor {
    Object intercept(Object source, Object[] args, InstanceMethodCaller caller) throws Throwable;
}
