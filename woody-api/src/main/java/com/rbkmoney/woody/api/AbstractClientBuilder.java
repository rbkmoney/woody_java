package com.rbkmoney.woody.api;

import com.rbkmoney.woody.api.event.ClientEvent;
import com.rbkmoney.woody.api.event.ClientEventListener;
import com.rbkmoney.woody.api.generator.IdGenerator;
import com.rbkmoney.woody.api.proxy.InvocationTargetProvider;
import com.rbkmoney.woody.api.proxy.MethodCallTracer;
import com.rbkmoney.woody.api.proxy.ProxyFactory;
import com.rbkmoney.woody.api.proxy.SingleTargetProvider;
import com.rbkmoney.woody.api.trace.context.*;

import java.net.URI;

/**
 * Created by vpankrashkin on 25.04.16.
 */
public abstract class AbstractClientBuilder implements ClientBuilder {
    private static final ClientEventListener DEFAULT_EVENT_LISTENER = (ClientEventListener<ClientEvent>) event -> {
    };

    private URI address;
    private IdGenerator idGenerator;
    private ClientEventListener eventListener = DEFAULT_EVENT_LISTENER;

    @Override
    public ClientBuilder withAddress(URI address) {
        this.address = address;
        return this;
    }

    @Override
    public ClientBuilder withEventListener(ClientEventListener listener) {
        this.eventListener = listener;
        return this;
    }

    @Override
    public ClientBuilder withIdGenerator(IdGenerator generator) {
        this.idGenerator = generator;
        return this;
    }

    @Override
    public URI getAddress() {
        return address;
    }

    @Override
    public ClientEventListener getEventListener() {
        return eventListener;
    }

    @Override
    public IdGenerator getIdGenerator() {
        return idGenerator;
    }

    @Override
    public <T> T build(Class<T> iface) {
        try {
            T target = createProviderClient(iface);
            return build(iface, new SingleTargetProvider<>(iface, target));
        } catch (WoodyInstantiationException e) {
            throw e;
        } catch (Exception e) {
            throw new WoodyInstantiationException(e);
        }
    }

    public <T> T build(Class<T> iface, InvocationTargetProvider<T> targetProvider) {
        try {
            return createProxyClient(iface, targetProvider);
        } catch (Exception e) {
            throw new WoodyInstantiationException(e);
        }
    }

    protected <T> T createProxyClient(Class<T> iface, InvocationTargetProvider<T> targetProvider) {
        return createProxyBuilder(iface).build(iface, targetProvider);
    }

    protected ProxyBuilder createProxyBuilder(Class iface) {
        ProxyBuilder proxyBuilder = new ProxyBuilder();
        proxyBuilder.setIdGenerator(idGenerator);
        proxyBuilder.setStartEventListener(getOnCallStartEventListener());
        proxyBuilder.setEndEventListener(getOnCallEndEventListener());
        proxyBuilder.setErrEventListener(getErrorListener());
        proxyBuilder.setMetadataExtender(getOnCallMetadataExtender(iface));
        return proxyBuilder;
    }

    abstract protected Runnable getErrorListener();

    abstract protected Runnable getOnCallStartEventListener();

    abstract protected Runnable getOnSendEventListener();

    abstract protected Runnable getOnReceiveEventListener();

    abstract protected Runnable getOnCallEndEventListener();

    abstract protected MethodCallTracer getOnCallMetadataExtender(Class iface);

    abstract protected <T> T createProviderClient(Class<T> iface);

    protected static class ProxyBuilder {
        public static final int EVENT_DISABLE = 0b0;
        public static final int EVENT_AFTER_CONTEXT_INIT = 0b01;
        public static final int EVENT_BEFORE_CONTEXT_DESTROY = 0b10;
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
            return new ContextTracer(createTraceContext(), createEventTracer());
        }

        protected TraceContext createTraceContext() {
            return TraceContext.forClient(idGenerator,
                    hasFlag(EVENT_AFTER_CONTEXT_INIT, startEventPhases) ? startEventListener : listenerStub,
                    hasFlag(EVENT_BEFORE_CONTEXT_DESTROY, endEventPhases) ? endEventListener : listenerStub,
                    hasFlag(EVENT_BEFORE_CONTEXT_DESTROY, errorEventPhases) ? errEventListener : listenerStub);
        }

        protected MethodCallTracer createEventTracer() {
            return new CompositeTracer(MetadataTracer.forClient(),
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
