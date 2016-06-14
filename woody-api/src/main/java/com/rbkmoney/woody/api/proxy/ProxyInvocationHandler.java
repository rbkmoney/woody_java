package com.rbkmoney.woody.api.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.TreeMap;

public class ProxyInvocationHandler implements InvocationHandler {

    private final Map<Method, InstanceMethodCaller> callMap;
    private final MethodCallInterceptor callInterceptor;

    public ProxyInvocationHandler(Class iface, InvocationTargetProvider targetProvider, MethodCallerFactory callerFactory, MethodCallInterceptor callInterceptor) {
        this.callMap = createCallMap(targetProvider, iface, callerFactory);
        this.callInterceptor = callInterceptor;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        InstanceMethodCaller methodCaller = callMap.get(method);
        if (methodCaller != null) {
            return callInterceptor.intercept(args, callMap.get(method));
        } else {
            return method.invoke(proxy, args);
        }
    }

    private Map<Method, InstanceMethodCaller> createCallMap(InvocationTargetProvider targetProvider, Class iface, MethodCallerFactory callerFactory) {
        Class targetType = targetProvider.getTargetType();

        if (!iface.isAssignableFrom(targetType)) {
            throw new IllegalArgumentException("Target object class doesn't implement referred interface");
        }
        Map<Method, InstanceMethodCaller> callerMap = new TreeMap<>(MethodShadow.METHOD_COMPARATOR);
        Method[] targetIfaceMethods = MethodShadow.getShadowedMethods(targetType, iface);

        for (Method method : targetIfaceMethods) {
            callerMap.put(MethodShadow.getSameMethod(method, iface), callerFactory.getInstance(targetProvider, method));
        }
        return callerMap;
    }
}
