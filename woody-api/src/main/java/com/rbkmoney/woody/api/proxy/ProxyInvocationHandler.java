package com.rbkmoney.woody.api.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.BiFunction;

public final class ProxyInvocationHandler implements InvocationHandler {

    private final Map<Method, CallerBundle> callMap;
    private final InvocationTargetProvider targetProvider;

    public ProxyInvocationHandler(Class iface, InvocationTargetProvider targetProvider, MethodCallerFactory callerFactory, MethodCallInterceptor callInterceptor) {
        this.targetProvider = targetProvider;
        this.callMap = createCallMap(callInterceptor, targetProvider, iface, callerFactory);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        CallerBundle methodCallerBundle = callMap.get(method);
        if (methodCallerBundle != null) {
            return methodCallerBundle.interceptor.intercept(proxy, args, methodCallerBundle.caller);
        } else {
            return method.invoke(targetProvider.getTarget(), args);
        }
    }

    private Map<Method, CallerBundle> createCallMap(MethodCallInterceptor callInterceptor, InvocationTargetProvider targetProvider, Class iface, MethodCallerFactory callerFactory) {
        Class targetType = targetProvider.getTargetType();

        if (!iface.isAssignableFrom(targetType)) {
            throw new IllegalArgumentException("Target object class doesn't implement referred interface");
        }
        Map<Method, CallerBundle> callerMap = new TreeMap<>(MethodShadow.METHOD_COMPARATOR);
        Method[] targetIfaceMethods = MethodShadow.getShadowedMethods(targetType, iface);

        for (Method method : targetIfaceMethods) {
            callerMap.put(MethodShadow.getSameMethod(method, iface), new CallerBundle(callerFactory.getInstance(targetProvider, method), callInterceptor));
        }

        return addObjectMethods(iface, callerFactory, callerMap);
    }

    private Map<Method, CallerBundle> addObjectMethods(Class iface, MethodCallerFactory callerFactory, Map<Method, CallerBundle> callerMap) {
        SingleTargetProvider objTargetProvider = new SingleTargetProvider(Object.class, this);//ref leak on init, assume it's a trusted code
        MethodCallInterceptor directCallInterceptor = MethodCallInterceptors.directCallInterceptor();
        BiFunction<Object, Object, Object> targetExtractor = (src, stub) -> {
            InvocationHandler invocationHandler = null;
            try {
                invocationHandler = Proxy.getInvocationHandler(src);
                if (!(invocationHandler instanceof ProxyInvocationHandler)) {
                    invocationHandler = null;
                }
            } catch (IllegalArgumentException e) {
            }
            return invocationHandler == null ? stub : invocationHandler;
        };

        try {
            //it's expected that this handler is bound only for one dedicated proxy
            Method objMethod = objTargetProvider.getClass().getMethod("hashCode");
            callerMap.put(objMethod, new CallerBundle(
                    callerFactory.getInstance(objTargetProvider, objMethod,
                            (src, args) -> targetExtractor.apply(src, ProxyInvocationHandler.this).hashCode()),
                    directCallInterceptor
            ));
            objMethod = objTargetProvider.getClass().getMethod("toString");
            callerMap.put(objMethod, new CallerBundle(
                    callerFactory.getInstance(objTargetProvider, objMethod,
                            (src, args) -> iface.getName() + "@" + targetExtractor.apply(src, ProxyInvocationHandler.this).hashCode()),
                    directCallInterceptor
            ));

            objMethod = objTargetProvider.getClass().getMethod("equals", Object.class);
            callerMap.put(objMethod, new CallerBundle(callerFactory.getInstance(objTargetProvider, objMethod,
                    (src, args) -> targetExtractor.apply(args[0], null) == ProxyInvocationHandler.this),
                    directCallInterceptor
            ));
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Object methods're not found", e);
        }
        return callerMap;
    }

    private static class CallerBundle {
        private final InstanceMethodCaller caller;
        private final MethodCallInterceptor interceptor;

        public CallerBundle(InstanceMethodCaller caller, MethodCallInterceptor interceptor) {
            this.caller = caller;
            this.interceptor = interceptor;
        }

        public InstanceMethodCaller getCaller() {
            return caller;
        }

        public MethodCallInterceptor getInterceptor() {
            return interceptor;
        }
    }

}
