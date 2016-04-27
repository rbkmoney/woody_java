package com.rbkmoney.woody.api.proxy;

import com.rbkmoney.woody.api.trace.context.EventListenerTracer;
import org.junit.Test;

import static org.junit.Assert.assertSame;

/**
 * Created by vpankrashkin on 23.04.16.
 */
public class TestProxyInvocationFactory {
    @Test
    public void testString() {
        //Srv direct = () -> "test";
        Srv directImpl = new Srv() {

            @Override
            public String getString() {
                return "string";
            }
        };
        MethodCallTracer callTracer = new MethodCallTracer() {
            @Override
            public void beforeCall(Object[] args, InstanceMethodCaller caller) {
                //System.out.println("Before");
            }

            @Override
            public void afterCall(Object[] args, InstanceMethodCaller caller, Object result) {
                //System.out.println("After");
            }

            @Override
            public void callError(Object[] args, InstanceMethodCaller caller, Throwable error) {
                //System.out.println("Error:" + error);
                error.printStackTrace();
            }
        };

        MethodCallTracer wrappedCallTracer = new EventListenerTracer(callTracer);

        ProxyFactory reflectionProxyFactory = new ProxyFactory(new ReflectionMethodCallerFactory(), wrappedCallTracer, false);
        ProxyFactory handleProxyFactory = new ProxyFactory(new HandleMethodCallerFactory(), wrappedCallTracer, false);

        Srv directLambda = () -> "string";
        Srv refDirectProxy = reflectionProxyFactory.getInstance(Srv.class, directImpl);
        Srv refLambdaProxy = reflectionProxyFactory.getInstance(Srv.class, directLambda);
        Srv handleDirectProxy = handleProxyFactory.getInstance(Srv.class, directImpl);
        Srv handleLambdaProxy = handleProxyFactory.getInstance(Srv.class, directLambda);
        handleDirectProxy.getString();
        handleLambdaProxy.getString();
        for (int i = 0; i < 1000000; i++) {
            assertSame(directImpl.getString(), refDirectProxy.getString());
            assertSame(directImpl.getString(), refLambdaProxy.getString());
            assertSame(directImpl.getString(), handleDirectProxy.getString());
            assertSame(directImpl.getString(), handleLambdaProxy.getString());
        }

        long start = System.currentTimeMillis();
        for (int i = 0; i < 5000000; i++) {
            directImpl.getString();
        }
        System.out.println("Direct:" + (System.currentTimeMillis() - start));

        start = System.currentTimeMillis();
        for (int i = 0; i < 5000000; i++) {
            directLambda.getString();
        }
        System.out.println("Lambda:" + (System.currentTimeMillis() - start));

        start = System.currentTimeMillis();
        for (int i = 0; i < 5000000; i++) {
            refDirectProxy.getString();
        }
        System.out.println("Refl Direct roxy:" + (System.currentTimeMillis() - start));

        start = System.currentTimeMillis();
        for (int i = 0; i < 5000000; i++) {
            refLambdaProxy.getString();
        }
        System.out.println("Refl Lambda roxy:" + (System.currentTimeMillis() - start));


        start = System.currentTimeMillis();
        for (int i = 0; i < 5000000; i++) {
            handleDirectProxy.getString();
        }
        System.out.println("Handle Direct Proxy:" + (System.currentTimeMillis() - start));

        start = System.currentTimeMillis();
        for (int i = 0; i < 5000000; i++) {
            handleLambdaProxy.getString();
        }
        System.out.println("Handle Lambda Proxy:" + (System.currentTimeMillis() - start));

    }

    private interface Srv {
        String getString();
    }
}
