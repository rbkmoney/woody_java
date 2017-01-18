package com.rbkmoney.woody.api.proxy;

import com.rbkmoney.woody.api.proxy.tracer.MethodCallTracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by vpankrashkin on 22.04.16.
 */
public class MethodCallInterceptors {

    private static final Logger log = LoggerFactory.getLogger(MethodCallInterceptors.class);

    public static MethodCallInterceptor directCallInterceptor() {
        return (src, args, caller) -> caller.call(src, args);
    }

    public static MethodCallInterceptor trackedCallInterceptor(MethodCallTracer callTracer) {
        return (src, args, caller) -> {
            Object result;
            callTracer.beforeCall(args, caller);
            try {
                result = caller.call(src, args);
            } catch (Throwable t) {
                log.debug("Call Error", t);
                callTracer.callError(args, caller, t);
                throw t;
            }
            callTracer.afterCall(args, caller, result);
            return result;
        };
    }
}
