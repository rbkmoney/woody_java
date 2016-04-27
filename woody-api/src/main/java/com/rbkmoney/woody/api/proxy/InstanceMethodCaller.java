package com.rbkmoney.woody.api.proxy;

/**
 * Created by vpankrashkin on 22.04.16.
 */
@FunctionalInterface
public interface InstanceMethodCaller {
    Object call(Object[] args) throws Throwable;
}
