package com.rbkmoney.woody.thrift.impl.http;

import com.rbkmoney.woody.api.WoodyInstantiationException;
import com.rbkmoney.woody.api.proxy.InvocationTargetProvider;
import com.rbkmoney.woody.api.proxy.SpawnTargetProvider;

/**
 * Created by vpankrashkin on 09.06.16.
 *
 * This builder provides the ability to build thread-safe clients around not thread-safe Thrift clients.
 * It creates new Thrift client instance for every call which is dropped after call finishes.
 */
public class THSpawnClientBuilder extends THClientBuilder {

    @Override
    public <T> T build(Class<T> iface) throws WoodyInstantiationException {
        try {
            return build(iface, createTargetProvider(iface));
        } catch (WoodyInstantiationException e) {
            throw e;
        } catch (Exception e) {
            throw new WoodyInstantiationException(e);
        }
    }

    private <T> InvocationTargetProvider<T> createTargetProvider(Class<T> iface) {
            SpawnTargetProvider<T> targetProvider = new SpawnTargetProvider<>(iface, () -> createProviderClient(iface));
            return targetProvider;
    }

}
