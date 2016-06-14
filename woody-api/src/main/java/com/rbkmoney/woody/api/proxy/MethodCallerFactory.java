package com.rbkmoney.woody.api.proxy;

import java.lang.reflect.Method;

/**
 * Created by vpankrashkin on 22.04.16.
 */
public interface MethodCallerFactory {
    InstanceMethodCaller getInstance(InvocationTargetProvider targetProvider, Method method);
}
