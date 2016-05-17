package com.rbkmoney.woody.api.proxy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by vpankrashkin on 22.04.16.
 */
public class ReflectionMethodCallerFactory implements MethodCallerFactory {
    @Override
    public InstanceMethodCaller getInstance(Object target, Method method) {
        method.setAccessible(true);
        return new InstanceMethodCaller(method) {
            @Override
            public Object call(Object[] args) throws Throwable {
                try {
                    return method.invoke(target, args);
                } catch (InvocationTargetException e) {
                    throw e.getCause();
                }
            }
        };
    }
}
