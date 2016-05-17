package com.rbkmoney.woody.api.interceptor;

/**
 * Created by vpankrashkin on 28.04.16.
 */
public interface CommonInterceptor<ReqProvider, RespProvider> extends RequestInterceptor<ReqProvider>, ResponseInterceptor<RespProvider> {
}
