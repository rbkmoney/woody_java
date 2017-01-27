package com.rbkmoney.woody.api;

import com.rbkmoney.woody.api.event.ServiceEvent;
import com.rbkmoney.woody.api.event.ServiceEventListener;
import com.rbkmoney.woody.api.proxy.ProxyFactory;
import com.rbkmoney.woody.api.proxy.SingleTargetProvider;
import com.rbkmoney.woody.api.proxy.tracer.CompositeTracer;
import com.rbkmoney.woody.api.proxy.tracer.EventTracer;
import com.rbkmoney.woody.api.proxy.tracer.MethodCallTracer;
import com.rbkmoney.woody.api.proxy.tracer.TargetCallTracer;

/**
 * Created by vpankrashkin on 10.05.16.
 */
public abstract class AbstractServiceBuilder<Srv> implements ServiceBuilder<Srv> {
    private static final ServiceEventListener DEFAULT_EVENT_LISTENER = (ServiceEventListener<ServiceEvent>) event -> {
    };
    private boolean allowObjectProxyOverriding = false;

    private ServiceEventListener eventListener = DEFAULT_EVENT_LISTENER;

    @Override
    public ServiceBuilder withEventListener(ServiceEventListener listener) {
        this.eventListener = listener;
        return this;
    }

    @Override
    public <T> Srv build(Class<T> iface, T serviceHandler) {
        try {
            T target = createProxyService(iface, serviceHandler);
            return createProviderService(iface, target);
        } catch (Exception e) {
            throw new WoodyInstantiationException(e);
        }
    }

    @Override
    public ServiceEventListener getEventListener() {
        return eventListener;
    }

    protected <T> T createProxyService(Class<T> iface, T handler) {

        ProxyFactory proxyFactory = new ProxyFactory(createEventTracer(), allowObjectProxyOverriding);
        return proxyFactory.getInstance(iface, new SingleTargetProvider<T>(iface, handler));
    }

    protected MethodCallTracer createEventTracer() {
        return new CompositeTracer(
                TargetCallTracer.forServer(),
                new EventTracer(getOnCallStartEventListener(),
                        getOnCallEndEventListener(),
                        getErrorListener()
                ));
    }

    public void setAllowObjectProxyOverriding(boolean allowObjectProxyOverriding) {
        this.allowObjectProxyOverriding = allowObjectProxyOverriding;
    }

    abstract protected Runnable getErrorListener();

    abstract protected Runnable getOnCallStartEventListener();

    abstract protected Runnable getOnSendEventListener();

    abstract protected Runnable getOnReceiveEventListener();

    abstract protected Runnable getOnCallEndEventListener();

    abstract protected <T> Srv createProviderService(Class<T> serviceInterface, T handler);

}
