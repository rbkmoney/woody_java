package com.rbkmoney.woody.api.interceptor.ext;

@FunctionalInterface
public interface InterceptorExtension<Ctx extends ExtensionContext> {
    /**
     * @throws RuntimeException if any error occurs
     */
    void apply(Ctx extContext);
}
