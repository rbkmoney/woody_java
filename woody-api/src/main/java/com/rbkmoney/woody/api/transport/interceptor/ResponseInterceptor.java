package com.rbkmoney.woody.api.transport.interceptor;

import com.rbkmoney.woody.api.trace.AbstractSpan;

/**
 * Created by vpankrashkin on 22.04.16.
 */
public interface ResponseInterceptor<Context extends AbstractSpan, Transport> {
    void interceptResponse(Context context, Transport transport);
}
