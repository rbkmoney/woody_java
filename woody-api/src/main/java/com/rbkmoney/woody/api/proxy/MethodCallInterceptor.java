package com.rbkmoney.woody.api.proxy;

/**
 * Created by vpankrashkin on 22.04.16.
 */
@FunctionalInterface
public interface MethodCallInterceptor {
    Object intercept(Object[] args, InstanceMethodCaller caller) throws Throwable;
}
