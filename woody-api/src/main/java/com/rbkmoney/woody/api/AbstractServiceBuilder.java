package com.rbkmoney.woody.api;

import com.rbkmoney.woody.api.event.ServiceEventListener;
import com.rbkmoney.woody.api.generator.IdGenerator;
import com.rbkmoney.woody.api.proxy.MethodCallTracer;
import com.rbkmoney.woody.api.proxy.ProxyFactory;
import com.rbkmoney.woody.api.trace.context.CompositeTracer;
import com.rbkmoney.woody.api.trace.context.EmptyTracer;
import com.rbkmoney.woody.api.trace.context.EventTracer;
import com.rbkmoney.woody.api.trace.context.MetadataTracer;

/**
 * Created by vpankrashkin on 10.05.16.
 */
public abstract class AbstractServiceBuilder<Service> implements ServiceBuilder<Service> {
    private ServiceEventListener eventListener;
    private IdGenerator idGenerator;


    @Override
    public ServiceBuilder withEventListener(ServiceEventListener listener) {
        this.eventListener = listener;
        return this;
    }

    @Override
    public ServiceBuilder withIdGenerator(IdGenerator generator) {
        this.idGenerator = generator;
        return this;
    }

    protected IdGenerator getIdGenerator() {
        return idGenerator;
    }

    @Override
    public <T> Service build(Class<T> serviceInterface, T serviceHandler) {
        try {
            T target = createProxyService(serviceInterface, serviceHandler);
            return createProviderService(serviceInterface, target);
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


    protected <T> T createProxyService(Class<T> serviceInterface, T handler) {
        return createProxyBuilder(serviceInterface).build(serviceInterface, handler);
    }

    protected ProxyBuilder createProxyBuilder(Class serviceInterface) {
        ProxyBuilder proxyBuilder = new ProxyBuilder();
        proxyBuilder.setIdGenerator(idGenerator);
        proxyBuilder.setStartEventListener(getOnCallStartEventListener());
        proxyBuilder.setEndEventListener(getOnCallEndEventListener());
        proxyBuilder.setErrEventListener(getErrorListener());
        proxyBuilder.setMetadataExtender(getOnCallMetadataExtender(serviceInterface));
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
        private IdGenerator idGenerator;
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

        public void setIdGenerator(IdGenerator idGenerator) {
            this.idGenerator = idGenerator;
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

        public <T> T build(Class<T> serviceInterface, T target) {
            ProxyFactory proxyFactory = createProxyFactory();
            return proxyFactory.getInstance(serviceInterface, target);
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
                            errEventListener)
            );
        }

        private boolean hasFlag(int test, int flags) {
            return (test & flags) != 0;
        }
    }

}
