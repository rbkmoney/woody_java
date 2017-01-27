package com.rbkmoney.woody.api.interceptor.ext;

/**
 * Created by vpankrashkin on 13.12.16.
 */
@FunctionalInterface
public interface InterceptorExtension<Ctx extends ExtensionContext> {
    /**
     * @throws RuntimeException if any error occurs
     * */
    void apply(Ctx extContext);
}
