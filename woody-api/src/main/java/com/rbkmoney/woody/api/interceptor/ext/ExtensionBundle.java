package com.rbkmoney.woody.api.interceptor.ext;

/**
 * Created by vpankrashkin on 14.12.16.
 */
public class ExtensionBundle {
    private final ContextBundle clientBundle;
    private final ContextBundle serviceBundle;

    public static ExtensionBundle createClientExtBundle(ContextBundle clientBundle) {
        return createExtBundle(clientBundle, ContextBundle.createCtxStub());
    }

    public static ExtensionBundle createServiceExtBundle(ContextBundle serviceBundle) {
        return createExtBundle(ContextBundle.createCtxStub(), serviceBundle);
    }

    public static ExtensionBundle createExtBundle(ContextBundle clientBundle, ContextBundle serviceBundle) {
        return new ExtensionBundle(clientBundle, serviceBundle);
    }

    public static ExtensionBundle createExtBundle(ContextBundle commonBundle) {
        return new ExtensionBundle(commonBundle, commonBundle);
    }

    public ExtensionBundle(ContextBundle clientBundle, ContextBundle serviceBundle) {
        this.clientBundle = clientBundle;
        this.serviceBundle = serviceBundle;
    }

    public ContextBundle getClientBundle() {
        return clientBundle;
    }

    public ContextBundle getServiceBundle() {
        return serviceBundle;
    }

    public static class ContextBundle {
        private final InterceptorExtension requestExtension;
        private final InterceptorExtension responseExtension;

        public static ContextBundle createCtxStub() {
            return new ContextBundle(ctx -> {}, ctx -> {});
        }

        public static ContextBundle createCtxBundle(InterceptorExtension requestExtension, InterceptorExtension responseExtension) {
            return new ContextBundle(requestExtension, responseExtension);
        }

        public ContextBundle(InterceptorExtension requestExtension, InterceptorExtension responseExtension) {
            this.requestExtension = requestExtension;
            this.responseExtension = responseExtension;
        }

        public InterceptorExtension getRequestExtension() {
            return requestExtension;
        }

        public InterceptorExtension getResponseExtension() {
            return responseExtension;
        }
    }
}
