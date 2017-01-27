package com.rbkmoney.woody.thrift.impl.http.interceptor;

import com.rbkmoney.woody.api.interceptor.CommonInterceptor;
import com.rbkmoney.woody.api.interceptor.ext.ExtendableInterceptor;
import com.rbkmoney.woody.api.interceptor.ext.ExtensionBundle;
import com.rbkmoney.woody.api.interceptor.ext.ExtensionContext;
import com.rbkmoney.woody.api.trace.TraceData;
import com.rbkmoney.woody.thrift.impl.http.interceptor.ext.THCExtensionContext;
import com.rbkmoney.woody.thrift.impl.http.interceptor.ext.THSExtensionContext;
import com.rbkmoney.woody.thrift.impl.http.interceptor.ext.TransportExtensionBundles;

import java.util.Collections;
import java.util.List;

/**
 * Created by vpankrashkin on 29.04.16.
 */
public class THTransportInterceptor extends ExtendableInterceptor implements CommonInterceptor {
    private final boolean isClient;

    public THTransportInterceptor(boolean isClient, boolean isRequest) {
        this(Collections.emptyList(), isClient, isRequest);
    }

    public THTransportInterceptor(List<ExtensionBundle> extensionBundles, boolean isClient, boolean isRequest) {
        super(
                isCl -> TransportExtensionBundles.getExtensions(isCl),
                extensionBundles,
                isClient,
                isRequest
        );
        this.isClient = isClient;
    }

    @Override
    protected ExtensionContext createContext(TraceData traceData, Object providerContext, Object[] contextParams) {
        return isClient ? new THCExtensionContext(traceData, providerContext, contextParams) : new THSExtensionContext(traceData, providerContext, contextParams);
    }
}
