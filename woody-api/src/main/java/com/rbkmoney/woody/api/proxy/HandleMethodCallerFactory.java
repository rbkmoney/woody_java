package com.rbkmoney.woody.api.proxy;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;

/**
 * Created by vpankrashkin on 22.04.16.
 */
public class HandleMethodCallerFactory implements MethodCallerFactory {

    @Override
    public InstanceMethodCaller getInstance(InvocationTargetProvider targetProvider, Method method) {

        Object target = targetProvider.getTarget();
        try {
            MethodHandle mh = MethodHandles.lookup()
                    .findVirtual(target.getClass(), method.getName(), MethodType.methodType(method.getReturnType(), method.getParameterTypes())).asSpreader(Object[].class, method.getParameterCount());

            return new InstanceMethodCaller(method) {
                @Override
                public Object call(Object[] args) throws Throwable {

                    return mh.invokeWithArguments(target, args);

                }
            };

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            targetProvider.releaseTarget(target);
        }
    }

}
