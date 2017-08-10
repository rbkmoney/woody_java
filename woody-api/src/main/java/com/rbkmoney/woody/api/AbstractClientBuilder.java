package com.rbkmoney.woody.api;

import com.rbkmoney.woody.api.event.ClientEvent;
import com.rbkmoney.woody.api.event.ClientEventListener;
import com.rbkmoney.woody.api.flow.error.ErrorMapProcessor;
import com.rbkmoney.woody.api.flow.error.WErrorDefinition;
import com.rbkmoney.woody.api.generator.IdGenerator;
import com.rbkmoney.woody.api.proxy.InvocationTargetProvider;
import com.rbkmoney.woody.api.proxy.tracer.MethodCallTracer;
import com.rbkmoney.woody.api.proxy.ProxyFactory;
import com.rbkmoney.woody.api.proxy.SingleTargetProvider;
import com.rbkmoney.woody.api.proxy.tracer.*;
import com.rbkmoney.woody.api.trace.ContextSpan;
import com.rbkmoney.woody.api.trace.context.*;

import java.net.URI;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

/**
 * Created by vpankrashkin on 25.04.16.
 */
public abstract class AbstractClientBuilder implements ClientBuilder {
    protected static final ClientEventListener DEFAULT_EVENT_LISTENER = (ClientEventListener<ClientEvent>) event -> {
    };
    private int networkTimeout = 5000;
    private URI address;
    private IdGenerator idGenerator;
    private ClientEventListener eventListener = DEFAULT_EVENT_LISTENER;
    private boolean allowObjectProxyOverriding = false;
    private final AtomicBoolean used = new AtomicBoolean(false);


    public ClientBuilder withNetworkTimeout(int timeout) {
        this.networkTimeout = timeout;
        return this;
    }

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

    public int getNetworkTimeout() {
        return networkTimeout;
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
        if (!used.compareAndSet(false, true)) {
            throw new IllegalStateException("Builder already used");
        }
        try {
            T target = createProviderClient(iface);
            return build(iface, new SingleTargetProvider<>(iface, target));
        } catch (WoodyInstantiationException e) {
            throw e;
        } catch (Exception e) {
            throw new WoodyInstantiationException(e);
        }
    }

    protected  <T> T build(Class<T> iface, InvocationTargetProvider<T> targetProvider) {
        try {
            return createProxyClient(iface, targetProvider);
        } catch (Exception e) {
            throw new WoodyInstantiationException(e);
        }
    }

    protected void setAllowObjectProxyOverriding(boolean allowObjectProxyOverriding) {
        this.allowObjectProxyOverriding = allowObjectProxyOverriding;
    }

    protected <T> T createProxyClient(Class<T> iface, InvocationTargetProvider<T> targetProvider) {
        ProxyFactory proxyFactory =  new ProxyFactory(createCallTracer(iface, () -> {}), allowObjectProxyOverriding);

        return proxyFactory.getInstance(iface, targetProvider);
    }

    protected MethodCallTracer createCallTracer(Class iface, Runnable listenerStub) {
        TraceContext traceContext = createTraceContext();
        ErrorMapProcessor errorMapProcessor = createErrorMapProcessor(iface);
        BiConsumer<WErrorDefinition, ContextSpan> errDefConsumer = getErrorDefinitionConsumer();
        return new ContextTracer(traceContext,
                new CompositeTracer(
                        TargetCallTracer.forClient(),
                        new ErrorMappingTracer(errorMapProcessor, errDefConsumer),
                        new EventTracer(listenerStub, getOnCallEndEventListener(), getErrorListener()),
                        new ErrorGenTracer(errorMapProcessor)
                ));
    }

    protected TraceContext createTraceContext() {
        return TraceContext.forClient(idGenerator);
    }

    abstract protected BiConsumer<WErrorDefinition, ContextSpan> getErrorDefinitionConsumer();

    abstract protected Runnable getErrorListener();

    abstract protected Runnable getOnCallStartEventListener();

    abstract protected Runnable getOnSendEventListener();

    abstract protected Runnable getOnReceiveEventListener();

    abstract protected Runnable getOnCallEndEventListener();

    abstract protected ErrorMapProcessor createErrorMapProcessor(Class iface);

    abstract protected <T> T createProviderClient(Class<T> iface);
}
