package com.rbkmoney.woody.api;

import com.rbkmoney.woody.api.event.ClientEventListener;
import com.rbkmoney.woody.api.generator.IdGenerator;
import com.rbkmoney.woody.api.provider.ClientProviderControl;
import com.rbkmoney.woody.api.proxy.MethodCallTracer;
import com.rbkmoney.woody.api.proxy.ProxyFactory;
import com.rbkmoney.woody.api.trace.context.ContextTracer;
import com.rbkmoney.woody.api.trace.context.EventListenerTracer;
import com.rbkmoney.woody.api.trace.context.MetadataTracer;
import com.rbkmoney.woody.api.trace.context.TraceContext;

import java.net.URI;

/**
 * Created by vpankrashkin on 25.04.16.
 */
public abstract class AbstractClientBuilder implements ClientBuilder {
    private URI address;
    private ClientEventListener eventListener;
    private IdGenerator idGenerator;

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
    public <T> T build(Class<T> clientInterface) {
        return createProxyClient(clientInterface, null);
    }

    @Override
    public <T> T build(Class<T> clientInterface, ClientProviderControl providerControl) {
        T target = null;
        return createProxyClient(clientInterface, target);
    }

    protected <T> T createProxyClient(Class<T> clientInterface, T target) {
        ProxyBuilder proxyBuilder = new ProxyBuilder();
        proxyBuilder.setIdGenerator(idGenerator);
        proxyBuilder.setStartEventListener(getEventStartListener(eventListener));
        proxyBuilder.setEndEventListener(getEventEndListener(eventListener));
        proxyBuilder.setErrEventListener(getErrorListener(eventListener));
        proxyBuilder.setStartEventPhases(ProxyBuilder.BEFORE_CALL_START);
        proxyBuilder.setEndEventPhases(ProxyBuilder.BEFORE_CONTEXT_DESTROY);
        return proxyBuilder.build(clientInterface, target);

    }

    abstract protected Runnable getErrorListener(ClientEventListener eventListener);

    abstract protected Runnable getEventStartListener(ClientEventListener eventListener);

    abstract protected Runnable getEventEndListener(ClientEventListener eventListener);

    abstract <T> T createProxyTarget(Class<T> clientInterface, ClientEventListener listener, ClientProviderControl providerControl);

    protected static class ProxyBuilder {
        private static final int AFTER_CONTEXT_INIT = 0b01;
        private static final int BEFORE_CONTEXT_DESTROY = 0b10;
        private static final int BEFORE_CALL_START = 0b100;
        private static final int AFTER_CALL_END = 0b1000;

        private int startEventPhases;
        private int endEventPhases;
        private boolean allowObjectOverriding = false;

        private final Runnable stub = () -> {
        };
        private Runnable startEventListener;
        private Runnable endEventListener;
        private Runnable errEventListener;
        private IdGenerator idGenerator;

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

        public void setAllowObjectOverriding(boolean allowObjectOverriding) {
            this.allowObjectOverriding = allowObjectOverriding;
        }

        public void setStartEventPhases(int phases) {
            startEventPhases = phases;
        }

        public void setEndEventPhases(int phases) {
            endEventPhases = phases;
        }

        public ProxyFactory createProxyFactory() {
            return new ProxyFactory(createMethodCallTracer(), allowObjectOverriding);
        }

        public MethodCallTracer createMethodCallTracer() {
            return new ContextTracer(createTraceContext(), createEventTracer());
        }

        public TraceContext createTraceContext() {
            return TraceContext.forClient(idGenerator,
                    hasFlag(AFTER_CONTEXT_INIT, startEventPhases) ? startEventListener : stub,
                    hasFlag(BEFORE_CONTEXT_DESTROY, endEventPhases) ? endEventListener : startEventListener);
        }

        public EventListenerTracer createEventTracer() {
            return new EventListenerTracer(MetadataTracer.forClient(),
                    hasFlag(BEFORE_CALL_START, startEventPhases) ? startEventListener : stub,
                    hasFlag(AFTER_CALL_END, endEventPhases) ? endEventListener : stub,
                    errEventListener);
        }

        public <T> T build(Class<T> clientInterface, T target) {
            ProxyFactory proxyFactory = createProxyFactory();
            return proxyFactory.getInstance(clientInterface, target);
        }

        private boolean hasFlag(int test, int flags) {
            return (test & flags) != 0;
        }
    }
}
