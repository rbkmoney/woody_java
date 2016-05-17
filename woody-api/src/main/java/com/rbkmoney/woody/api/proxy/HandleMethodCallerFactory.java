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
    public InstanceMethodCaller getInstance(Object target, Method method) {


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
        }
    }

    private Object[] createArgList(Object target, Object[] args) {
        if (args == null || args.length == 0) {
            return new Object[]{target};
        } else {
            Object[] mArgs = new Object[args.length + 1];
            {
                mArgs[0] = target;
                System.arraycopy(args, 0, mArgs, 1, args.length);
                return mArgs;
            }
        }

    }
}
