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

//    public static MethodCallInterceptor trackedWithTimeoutCallInterceptor(MethodCallTracer callTracer) {
//        return (src, args, caller) -> trackedCallInterceptor(callTracer).intercept(src, args, new InstanceMethodCaller(caller.getTargetMethod()) {
//
//            @Override
//            public Object call(Object source, Object[] arguments) throws Throwable {
//                TraceData traceData = TraceContext.getCurrentTraceData();
//                FutureTask<Map.Entry<TraceData, Object>> futureResult = new FutureTask<>(
//                        WFlow.create(
//                                () -> {
//                                    try {
//                                        Object object = caller.call(source, arguments);
//                                        return new AbstractMap.SimpleEntry<>(TraceContext.getCurrentTraceData(), object);
//                                    } catch (Throwable throwable) {
//                                        throw new WExecutionException(throwable, TraceContext.getCurrentTraceData());
//                                    }
//                                }
//                        )
//                );
//                Thread thread = new Thread(futureResult);
//                thread.start();
//                try {
//                    Map.Entry<TraceData, Object> result = futureResult.get(ContextUtils.getExecutionTimeout(), TimeUnit.MILLISECONDS);
//                    traceData = result.getKey();
//                    return result.getValue();
//                } catch (TimeoutException ex) {
//                    futureResult.cancel(true);
//                    throw new WUnavailableResultException("deadline reached");
//                } catch (ExecutionException ex) {
//                    Throwable throwable = ex.getCause();
//                    if (throwable instanceof WExecutionException) {
//                        traceData = ((WExecutionException) throwable).getTraceData();
//                    }
//                    throw throwable.getCause();
//                } catch (InterruptedException ex) {
//                    Thread.currentThread().interrupt();
//                    throw ex;
//                } finally {
//                    TraceContext.setCurrentTraceData(new TraceData(traceData, true));
//                }
//            }
//        });
//
//    }
}
