package com.rbkmoney.woody.api.interceptor.ext;

import com.rbkmoney.woody.api.interceptor.Interceptor;
import com.rbkmoney.woody.api.trace.TraceData;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by vpankrashkin on 13.12.16.
 */
public class ExtendableInterceptor implements Interceptor {
    private final InterceptorExtension[] extensions;
    private final BiFunction<TraceData, Throwable, Boolean> errContextResolver;

    public static List<ExtensionBundle> concatBundleLists(List<ExtensionBundle> first, List<ExtensionBundle> second) {
        return Stream.concat(first.stream(), second.stream()).collect(Collectors.toList());
    }

    public ExtendableInterceptor(List<InterceptorExtension> extensions, BiFunction<TraceData, Throwable, Boolean> errContextResolver) {
        this.extensions = extensions.toArray(new InterceptorExtension[extensions.size()]);
        this.errContextResolver = errContextResolver;
    }

    public ExtendableInterceptor(List<ExtensionBundle> extensionBundles, BiFunction<TraceData, Throwable, Boolean> errContextResolver, boolean isClient, boolean isRequest) {
        this(extensionBundles.stream()
                        .map(bundle -> {
                            ExtensionBundle.ContextBundle ctxBundle = isClient ? bundle.getClientBundle() : bundle.getServiceBundle();
                            return isRequest ? ctxBundle.getRequestExtension() : ctxBundle.getResponseExtension();
                        })
                        .collect(Collectors.toList()),
                errContextResolver
        );
    }

    public ExtendableInterceptor(Function<Boolean, List<ExtensionBundle>> primaryBundlesProvider, List<ExtensionBundle> secondaryBundles, boolean isRequest, boolean isClient) {
        this(
                concatBundleLists(
                        primaryBundlesProvider.apply(isClient),
                        secondaryBundles),
                (traceData, throwable) -> isClient,
                isClient,
                isRequest
        );
    }

    @Override
    public boolean intercept(TraceData traceData, Object providerContext, Object... contextParams) {
        try {
            ExtensionContext extContext = createContext(traceData, providerContext, contextParams);
            initInterception(extContext);
            for (int i = 0; i < extensions.length; ++i) {
                extensions[i].apply(extContext);
            }
            finalizeInterception(extContext);
            return true;
        } catch (Exception e) {
            return interceptError(traceData, e, errContextResolver.apply(traceData, e));
        }
    }

    protected ExtensionContext createContext(TraceData traceData, Object providerContext, Object[] contextParams) {
        return new ExtensionContext(traceData, providerContext, contextParams);
    }

    protected void initInterception(ExtensionContext extContext) {
    }

    protected void finalizeInterception(ExtensionContext extContext) {
    }
}
