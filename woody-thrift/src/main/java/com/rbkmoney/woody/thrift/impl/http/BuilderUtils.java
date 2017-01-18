package com.rbkmoney.woody.thrift.impl.http;

import com.rbkmoney.woody.api.interceptor.CommonInterceptor;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolFactory;

import java.util.function.BiFunction;

/**
 * Created by vpankrashkin on 17.01.17.
 */
class BuilderUtils {
    private static final BiFunction<TProtocolFactory, CommonInterceptor, TProtocolFactory> T_PROT_FACTORY_FUNC = (factory, protInterceptor) ->
            tTransport -> {
                TProtocol tProtocol = factory.getProtocol(tTransport);
                return new THSProtocolWrapper(tProtocol, protInterceptor);
            };

    static TProtocolFactory wrapProtocolFactory(TProtocolFactory protocolFactory, CommonInterceptor interceptor) {
        return T_PROT_FACTORY_FUNC.apply(protocolFactory, interceptor);
    }
}
