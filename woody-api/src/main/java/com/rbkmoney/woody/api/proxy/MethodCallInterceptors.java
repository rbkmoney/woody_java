package com.rbkmoney.woody.api.proxy;

/**
 * Created by vpankrashkin on 22.04.16.
 */
public class MethodCallInterceptors {
    public static MethodCallInterceptor directCallInterceptor() {
        return (args, caller) -> caller.call(args);
    }

    public static MethodCallInterceptor trackedCallInterceptor(MethodCallTracer callTracer) {
        return (args, caller) -> {
            callTracer.beforeCall(args, caller);
            try {
                Object result = caller.call(args);
                callTracer.afterCall(args, caller, result);
                return result;
            } catch (Throwable t) {
                callTracer.callError(args, caller, t);
                throw t;
            }
        };
    }
}
