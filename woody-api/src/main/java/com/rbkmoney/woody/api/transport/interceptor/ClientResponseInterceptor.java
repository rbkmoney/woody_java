package com.rbkmoney.woody.api.transport.interceptor;

import com.rbkmoney.woody.api.trace.ServerSpan;

/**
 * Created by vpankrashkin on 22.04.16.
 */
public interface ClientResponseInterceptor<Transport> extends ResponseInterceptor<ServerSpan, Transport> {
}
