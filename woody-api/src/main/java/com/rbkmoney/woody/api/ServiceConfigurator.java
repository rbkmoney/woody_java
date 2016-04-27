package com.rbkmoney.woody.api;

/**
 * Created by vpankrashkin on 22.04.16.
 */
public interface ServiceConfigurator<T> {
    void configure(T serviceProvider);
}
