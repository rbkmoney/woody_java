package com.rbkmoney.woody.api.proxy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectionMethodCallerFactory implements MethodCallerFactory {
    @Override
    public InstanceMethodCaller getInstance(InvocationTargetProvider targetProvider, Method method) {
        method.setAccessible(true);
        return new InstanceMethodCaller(method) {
            @Override
            public Object call(Object source, Object[] args) throws Throwable {
                Object target = targetProvider.getTarget();
                try {
                    try {
                        return method.invoke(target, args);
                    } finally {
                        targetProvider.releaseTarget(target);
                    }
                } catch (InvocationTargetException e) {
                    Throwable cause = e.getCause();
                    throw cause == null ? e : cause;
                }
            }
        };
    }
}
