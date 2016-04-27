package com.rbkmoney.woody.api.transport.interceptor;

import com.rbkmoney.woody.api.trace.ClientSpan;

/**
 * Created by vpankrashkin on 22.04.16.
 */
public interface ClientRequestInterceptor<Transport> extends RequestInterceptor<ClientSpan, Transport> {
}
