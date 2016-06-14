package com.rbkmoney.woody.api;

import com.rbkmoney.woody.api.event.ServiceEvent;
import com.rbkmoney.woody.api.event.ServiceEventListener;
import com.rbkmoney.woody.api.proxy.InvocationTargetProvider;
import com.rbkmoney.woody.api.proxy.MethodCallTracer;
import com.rbkmoney.woody.api.proxy.ProxyFactory;
import com.rbkmoney.woody.api.proxy.SingleTargetProvider;
import com.rbkmoney.woody.api.trace.context.CompositeTracer;
import com.rbkmoney.woody.api.trace.context.EmptyTracer;
import com.rbkmoney.woody.api.trace.context.EventTracer;
import com.rbkmoney.woody.api.trace.context.MetadataTracer;

/**
 * Created by vpankrashkin on 10.05.16.
 */
public abstract class AbstractServiceBuilder<Service> implements ServiceBuilder<Service> {
    private static final ServiceEventListener DEFAULT_EVENT_LISTENER = (ServiceEventListener<ServiceEvent>) event -> {
    };

    private ServiceEventListener eventListener = DEFAULT_EVENT_LISTENER;

    @Override
    public ServiceBuilder withEventListener(ServiceEventListener listener) {
        this.eventListener = listener;
        return this;
    }

    @Override
    public <T> Service build(Class<T> iface, T serviceHandler) {
        try {
            T target = createProxyService(iface, serviceHandler);
            return createProviderService(iface, target);
        } catch (Exception e) {
            throw new WoodyInstantiationException(e);
        }
    }

    protected ServiceEventListener getEventListener() {
        return eventListener;
    }

    abstract protected Runnable getErrorListener();

    abstract protected Runnable getOnCallStartEventListener();

    abstract protected Runnable getOnSendEventListener();

    abstract protected Runnable getOnReceiveEventListener();

    abstract protected Runnable getOnCallEndEventListener();

    abstract protected MethodCallTracer getOnCallMetadataExtender(Class serviceInterface);

    abstract protected <T> Service createProviderService(Class<T> serviceInterface, T handler);


    protected <T> T createProxyService(Class<T> iface, T handler) {
        return createProxyBuilder(iface).build(iface, handler);
    }

    protected ProxyBuilder createProxyBuilder(Class iface) {
        ProxyBuilder proxyBuilder = new ProxyBuilder();
        proxyBuilder.setStartEventListener(getOnCallStartEventListener());
        proxyBuilder.setEndEventListener(getOnCallEndEventListener());
        proxyBuilder.setErrEventListener(getErrorListener());
        proxyBuilder.setMetadataExtender(getOnCallMetadataExtender(iface));
        return proxyBuilder;
    }

    protected static class ProxyBuilder {
        public static final int EVENT_DISABLE = 0b0;
        public static final int EVENT_BEFORE_CALL_START = 0b100;
        public static final int EVENT_AFTER_CALL_END = 0b1000;

        private int startEventPhases;
        private int endEventPhases;
        private int errorEventPhases;
        private boolean allowObjectOverriding = false;

        private final Runnable listenerStub = () -> {
        };

        private final MethodCallTracer extenderStub = new EmptyTracer();

        private Runnable startEventListener;
        private Runnable endEventListener;
        private Runnable errEventListener;
        private MethodCallTracer metadataExtender;

        public void setStartEventListener(Runnable startEventListener) {
            this.startEventListener = startEventListener;
        }

        public void setEndEventListener(Runnable endEventListener) {
            this.endEventListener = endEventListener;
        }

        public void setErrEventListener(Runnable errEventListener) {
            this.errEventListener = errEventListener;
        }

        public void setMetadataExtender(MethodCallTracer metadataExtender) {
            this.metadataExtender = metadataExtender;
        }

        public void setAllowObjectOverriding(boolean allowObjectOverriding) {
            this.allowObjectOverriding = allowObjectOverriding;
        }

        public void setStartEventPhases(int phases) {
            startEventPhases = phases;
        }

        public void setEndEventPhases(int phases) {
            endEventPhases = phases;
        }

        public void setErrorEventPhases(int phases) {
            this.errorEventPhases = phases;
        }

        public <T> T build(Class<T> iface, T target) {
            return build(iface, new SingleTargetProvider<>(iface, target));
        }

        public <T> T build(Class<T> iface, InvocationTargetProvider<T> targetProvider) {
            ProxyFactory proxyFactory = createProxyFactory();
            return proxyFactory.getInstance(iface, targetProvider);
        }

        protected ProxyFactory createProxyFactory() {
            return new ProxyFactory(createMethodCallTracer(), allowObjectOverriding);
        }

        protected MethodCallTracer createMethodCallTracer() {
            return createEventTracer();
        }

        protected MethodCallTracer createEventTracer() {
            return new CompositeTracer(MetadataTracer.forServer(),
                    metadataExtender == null ? extenderStub : metadataExtender,
                    new EventTracer(
                            hasFlag(EVENT_BEFORE_CALL_START, startEventPhases) ? startEventListener : listenerStub,
                            hasFlag(EVENT_AFTER_CALL_END, endEventPhases) ? endEventListener : listenerStub,
                            hasFlag(EVENT_AFTER_CALL_END, errorEventPhases) ? errEventListener : listenerStub)
            );
        }

        private boolean hasFlag(int test, int flags) {
            return (test & flags) != 0;
        }
    }

}
