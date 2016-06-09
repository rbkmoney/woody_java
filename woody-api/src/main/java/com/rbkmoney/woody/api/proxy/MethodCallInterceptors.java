package com.rbkmoney.woody.api.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by vpankrashkin on 22.04.16.
 */
public class MethodCallInterceptors {

    private static Logger log = LoggerFactory.getLogger(MethodCallInterceptors.class);

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
                log.trace("Call Error", t);
                callTracer.callError(args, caller, t);
                throw t;
            }
        };
    }
}
