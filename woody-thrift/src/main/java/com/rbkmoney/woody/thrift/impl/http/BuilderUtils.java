package com.rbkmoney.woody.thrift.impl.http;

import com.rbkmoney.woody.api.interceptor.CommonInterceptor;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolFactory;

import java.util.function.BiFunction;

class BuilderUtils {

    static TProtocolFactory wrapProtocolFactory(TProtocolFactory protocolFactory, CommonInterceptor interceptor, boolean isClient) {
        BiFunction<TProtocolFactory, CommonInterceptor, TProtocolFactory> tProtocolFactoryFunc = (factory, protInterceptor) ->
                tTransport -> {
                    TProtocol tProtocol = factory.getProtocol(tTransport);
                    return new THSProtocolWrapper(tProtocol, protInterceptor, isClient);
                };

        return tProtocolFactoryFunc.apply(protocolFactory, interceptor);
    }
}
