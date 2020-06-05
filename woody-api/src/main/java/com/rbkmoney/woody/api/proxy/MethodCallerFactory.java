package com.rbkmoney.woody.api.proxy;

import java.lang.reflect.Method;
import java.util.function.BiFunction;

public interface MethodCallerFactory {
    InstanceMethodCaller getInstance(InvocationTargetProvider targetProvider, Method method);

    default InstanceMethodCaller getInstance(InvocationTargetProvider targetProvider, Method method, BiFunction<Object, Object[], Object> function) {
        return new InstanceMethodCaller(method) {
            @Override
            public Object call(Object source, Object[] args) throws Throwable {
                return function.apply(source, args);
            }
        };
    }
}
