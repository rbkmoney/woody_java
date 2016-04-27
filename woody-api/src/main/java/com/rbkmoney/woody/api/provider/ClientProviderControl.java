package com.rbkmoney.woody.api.provider;

/**
 * Created by vpankrashkin on 22.04.16.
 */
public interface ClientProviderControl<T> {
    void configure(T clientProvider);
}
