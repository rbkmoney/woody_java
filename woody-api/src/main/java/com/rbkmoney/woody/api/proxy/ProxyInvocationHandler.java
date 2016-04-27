package com.rbkmoney.woody.api.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.TreeMap;

public class ProxyInvocationHandler implements InvocationHandler {

    private final Map<Method, InstanceMethodCaller> callMap;
    private final MethodCallInterceptor callInterceptor;
    private final Object target;

    public ProxyInvocationHandler(Object target, Class iface, MethodCallerFactory callerFactory, MethodCallInterceptor callInterceptor) {
        this.callMap = createCallMap(target, iface, callerFactory);
        this.callInterceptor = callInterceptor;
        this.target = target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        InstanceMethodCaller methodCaller = callMap.get(method);
        if (methodCaller != null) {
            return callInterceptor.intercept(args, callMap.get(method));
        } else {
            return method.invoke(target, args);
        }
    }

    private Map<Method, InstanceMethodCaller> createCallMap(Object target, Class iface, MethodCallerFactory callerFactory) {
        if (!iface.isAssignableFrom(target.getClass())) {
            throw new IllegalArgumentException("Target object class doesn't implement referred interface");
        }
        Map<Method, InstanceMethodCaller> callerMap = new TreeMap<>(MethodShadow.METHOD_COMPARATOR);
        Method[] targetIfaceMethods = MethodShadow.getShadowedMethods(target, iface);

        for (Method method : targetIfaceMethods) {
            callerMap.put(MethodShadow.getSameMethod(method, iface), callerFactory.getInstance(target, method));
        }
        return callerMap;
    }
}
