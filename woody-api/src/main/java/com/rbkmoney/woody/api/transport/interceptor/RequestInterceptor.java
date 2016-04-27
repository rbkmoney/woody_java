package com.rbkmoney.woody.api.transport.interceptor;

import com.rbkmoney.woody.api.trace.AbstractSpan;

/**
 * Created by vpankrashkin on 22.04.16.
 */
public interface RequestInterceptor<Context extends AbstractSpan, Transport> {
    boolean interceptRequest(Context context, Transport spanContext);
}
