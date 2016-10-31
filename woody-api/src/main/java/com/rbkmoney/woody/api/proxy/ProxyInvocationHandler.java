package com.rbkmoney.woody.api.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.BiFunction;

public final class ProxyInvocationHandler implements InvocationHandler {

    private final Map<Method, InstanceMethodCaller> callMap;
    private final MethodCallInterceptor callInterceptor;
    private final InvocationTargetProvider targetProvider;

    public ProxyInvocationHandler(Class iface, InvocationTargetProvider targetProvider, MethodCallerFactory callerFactory, MethodCallInterceptor callInterceptor) {
        this.targetProvider = targetProvider;
        this.callInterceptor = callInterceptor;
        this.callMap = createCallMap(targetProvider, iface, callerFactory);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        InstanceMethodCaller methodCaller = callMap.get(method);
        if (methodCaller != null) {
            return callInterceptor.intercept(proxy, args, callMap.get(method));
        } else {
            return method.invoke(targetProvider.getTarget(), args);
        }
    }

    private Map<Method, InstanceMethodCaller> createCallMap(InvocationTargetProvider targetProvider, Class iface, MethodCallerFactory callerFactory) {
        Class targetType = targetProvider.getTargetType();

        if (!iface.isAssignableFrom(targetType)) {
            throw new IllegalArgumentException("Target object class doesn't implement referred interface");}
        Map<Method, InstanceMethodCaller> callerMap = new TreeMap<>(MethodShadow.METHOD_COMPARATOR);
        Method[] targetIfaceMethods = MethodShadow.getShadowedMethods(targetType, iface);

        for (Method method : targetIfaceMethods) {
            callerMap.put(MethodShadow.getSameMethod(method, iface), callerFactory.getInstance(targetProvider, method));
        }

        return addObjectMethods(iface, callerFactory, callerMap);
    }

    private Map<Method, InstanceMethodCaller> addObjectMethods(Class iface, MethodCallerFactory callerFactory, Map<Method, InstanceMethodCaller> callerMap) {
        SingleTargetProvider objTargetProvider = new SingleTargetProvider(Object.class, this);//ref leak on init, assume it's a trusted code
        BiFunction<Object, Object, Object> targetExtractor = (src, stub) -> {
            InvocationHandler invocationHandler = null;
            try {
                invocationHandler = Proxy.getInvocationHandler(src);
                if (!(invocationHandler instanceof ProxyInvocationHandler)) {
                    invocationHandler = null;
                }
            } catch (IllegalArgumentException e) {}
            return invocationHandler == null ? stub : invocationHandler;
        };

        try {
            //it's expected that this handler is bound only for one dedicated proxy
            Method objMethod = objTargetProvider.getClass().getMethod("hashCode");
            callerMap.putIfAbsent(objMethod, callerFactory.getInstance(objTargetProvider, objMethod,
                    (src, args) -> targetExtractor.apply(src, ProxyInvocationHandler.this).hashCode()));
            objMethod = objTargetProvider.getClass().getMethod("toString");
            callerMap.putIfAbsent(objMethod, callerFactory.getInstance(objTargetProvider, objMethod,
                    (src, args) -> iface.getName()+ "@" + targetExtractor.apply(src, ProxyInvocationHandler.this).hashCode()));

            objMethod = objTargetProvider.getClass().getMethod("equals", Object.class);
            callerMap.putIfAbsent(objMethod, callerFactory.getInstance(objTargetProvider, objMethod,
                    (src, args) -> targetExtractor.apply(args[0], null) == ProxyInvocationHandler.this));
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Object methods're not found", e);
        }
        return callerMap;
    }

}
