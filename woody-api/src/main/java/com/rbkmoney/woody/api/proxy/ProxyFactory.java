package com.rbkmoney.woody.api.proxy;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Created by vpankrashkin on 22.04.16.
 */
public class ProxyFactory {
    private final Object object = new Object();
    private final MethodCallerFactory callerFactory;
    private final MethodCallTracer callTracer;
    private final boolean allowObjectOverriding;

    public ProxyFactory(MethodCallTracer callTracer, boolean allowObjectOverriding) {
        this(new ReflectionMethodCallerFactory(), callTracer, allowObjectOverriding);
    }

    public ProxyFactory(MethodCallerFactory callerFactory, MethodCallTracer callTracer, boolean allowObjectOverriding) {
        this.callerFactory = callerFactory;
        this.callTracer = callTracer;
        this.allowObjectOverriding = allowObjectOverriding;
    }

    public <T> T getInstance(Class<T> iface, T target) {
        return getInstance(iface, target, callerFactory, callTracer, allowObjectOverriding);
    }

    @SuppressWarnings("unchecked")
    public <T> T getInstance(Class<T> iface, T target, MethodCallerFactory callerFactory, MethodCallTracer callTracer, boolean allowObjectOverriding) {
        if (!allowObjectOverriding) {
            Method[] overriden = MethodShadow.getShadowedMethods(object, iface);
            if (overriden.length != 0) {
                throw new IllegalArgumentException("Target interface " + iface.getName() + "shadows Object methods:" + overriden);
            }
        }
        return makeProxy(target, iface, callerFactory, callTracer);

    }

    protected <T> T makeProxy(T target, Class<T> iface, MethodCallerFactory callerFactory, MethodCallTracer callTracer) {

        return (T) Proxy.newProxyInstance(
                iface.getClassLoader(),
                new Class[]{iface},
                new ProxyInvocationHandler(target, iface, callerFactory, MethodCallInterceptors.trackedCallInterceptor(callTracer)));

    }
}
