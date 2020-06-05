package com.rbkmoney.woody.api;

import com.rbkmoney.woody.api.event.ServiceEvent;
import com.rbkmoney.woody.api.event.ServiceEventListener;
import com.rbkmoney.woody.api.proxy.ProxyFactory;
import com.rbkmoney.woody.api.proxy.SingleTargetProvider;
import com.rbkmoney.woody.api.proxy.tracer.*;

import java.util.concurrent.atomic.AtomicBoolean;

public abstract class AbstractServiceBuilder<Srv> implements ServiceBuilder<Srv> {
    protected static final ServiceEventListener DEFAULT_EVENT_LISTENER = (ServiceEventListener<ServiceEvent>) event -> {
    };
    private boolean allowObjectProxyOverriding = false;

    private ServiceEventListener eventListener = DEFAULT_EVENT_LISTENER;
    private final AtomicBoolean used = new AtomicBoolean(false);

    @Override
    public ServiceBuilder withEventListener(ServiceEventListener listener) {
        this.eventListener = listener;
        return this;
    }

    @Override
    public <T> Srv build(Class<T> iface, T serviceHandler) {
        if (!used.compareAndSet(false, true)) {
            throw new IllegalStateException("Builder already used");
        }
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
                DeadlineTracer.forService(),
                new EventTracer(getOnCallStartEventListener(),
                        getOnCallEndEventListener(),
                        getErrorListener()
                        ));
    }

    public void setAllowObjectProxyOverriding(boolean allowObjectProxyOverriding) {
        this.allowObjectProxyOverriding = allowObjectProxyOverriding;
    }

    protected abstract Runnable getErrorListener();

    protected abstract Runnable getOnCallStartEventListener();

    protected abstract Runnable getOnSendEventListener();

    protected abstract Runnable getOnReceiveEventListener();

    protected abstract Runnable getOnCallEndEventListener();

    protected abstract <T> Srv createProviderService(Class<T> serviceInterface, T handler);

}
