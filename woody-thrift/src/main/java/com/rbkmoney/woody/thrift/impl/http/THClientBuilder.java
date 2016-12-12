package com.rbkmoney.woody.thrift.impl.http;

import com.rbkmoney.woody.api.AbstractClientBuilder;
import com.rbkmoney.woody.api.event.ClientEventListener;
import com.rbkmoney.woody.api.flow.WFlow;
import com.rbkmoney.woody.api.generator.IdGenerator;
import com.rbkmoney.woody.api.interceptor.CommonInterceptor;
import com.rbkmoney.woody.api.interceptor.CompositeInterceptor;
import com.rbkmoney.woody.api.interceptor.ContainerCommonInterceptor;
import com.rbkmoney.woody.api.provider.ProviderEventInterceptor;
import com.rbkmoney.woody.api.proxy.InstanceMethodCaller;
import com.rbkmoney.woody.api.proxy.MethodCallTracer;
import com.rbkmoney.woody.api.trace.context.EmptyTracer;
import com.rbkmoney.woody.api.trace.context.TraceContext;
import com.rbkmoney.woody.api.transport.TransportEventInterceptor;
import com.rbkmoney.woody.thrift.impl.http.event.THClientEvent;
import com.rbkmoney.woody.thrift.impl.http.interceptor.THCMessageRequestInterceptor;
import com.rbkmoney.woody.thrift.impl.http.interceptor.THCMessageResponseInterceptor;
import com.rbkmoney.woody.thrift.impl.http.interceptor.THCRequestInterceptor;
import com.rbkmoney.woody.thrift.impl.http.interceptor.THCResponseInterceptor;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.thrift.TServiceClient;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.THttpClient;
import org.apache.thrift.transport.TTransport;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.Arrays;
import java.util.Optional;

/**
 * Created by vpankrashkin on 28.04.16.
 */
public class THClientBuilder extends AbstractClientBuilder {

    private HttpClient httpClient;

    public THClientBuilder() {
        this.httpClient = createHttpClient();
        super.withIdGenerator(WFlow.createDefaultIdGenerator());
    }

    public THClientBuilder withHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
        return this;
    }

    @Override
    public THClientBuilder withAddress(URI address) {
        return (THClientBuilder) super.withAddress(address);
    }

    @Override
    public THClientBuilder withEventListener(ClientEventListener listener) {
        return (THClientBuilder) super.withEventListener(listener);
    }

    @Override
    public THClientBuilder withIdGenerator(IdGenerator generator) {
        return (THClientBuilder) super.withIdGenerator(generator);
    }

    public HttpClient getHttpClient() {
        return httpClient;
    }

    @Override
    protected MethodCallTracer getOnCallMetadataExtender(Class iface) {
        return new EmptyTracer() {
            THErrorMetadataExtender metadataExtender = new THErrorMetadataExtender(iface);

            @Override
            public void callError(Object[] args, InstanceMethodCaller caller, Throwable error) {
                metadataExtender.extendClientError(TraceContext.getCurrentTraceData());
            }
        };
    }

    @Override
    protected Runnable getErrorListener() {
        return createEventRunnable(getEventListener());
    }

    @Override
    protected Runnable getOnCallStartEventListener() {
        return createEventRunnable(getEventListener());
    }

    @Override
    protected Runnable getOnCallEndEventListener() {
        return createEventRunnable(getEventListener());
    }

    @Override
    protected Runnable getOnSendEventListener() {
        return createEventRunnable(getEventListener());
    }

    @Override
    protected Runnable getOnReceiveEventListener() {
        return createEventRunnable(getEventListener());
    }

    @Override
    protected <T> T createProviderClient(Class<T> iface) {
        try {
            THttpClient tHttpClient = new THttpClient(getAddress().toString(), getHttpClient(), createTransportInterceptor());
            TProtocol tProtocol = createProtocol(tHttpClient);
            return createThriftClient(iface, tProtocol, createMessageInterceptor());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected ProxyBuilder createProxyBuilder(Class iface) {
        ProxyBuilder proxyBuilder = super.createProxyBuilder(iface);
        proxyBuilder.setStartEventPhases(ProxyBuilder.EVENT_DISABLE);
        proxyBuilder.setEndEventPhases(ProxyBuilder.EVENT_BEFORE_CONTEXT_DESTROY);
        proxyBuilder.setErrorEventPhases(ProxyBuilder.EVENT_AFTER_CALL_END);
        return proxyBuilder;
    }

    protected TProtocol createProtocol(TTransport tTransport) {
        return new TBinaryProtocol(tTransport);
    }

    protected HttpClient createHttpClient() {
        return HttpClientBuilder.create().build();
    }

    protected CommonInterceptor createMessageInterceptor() {
        return new CompositeInterceptor(
                new ContainerCommonInterceptor(new THCMessageRequestInterceptor(), new THCMessageResponseInterceptor()),
                new ProviderEventInterceptor(getOnCallStartEventListener(), null)
        );
    }

    protected CommonInterceptor createTransportInterceptor() {
        return new CompositeInterceptor(
                new ContainerCommonInterceptor(new THCRequestInterceptor(), new THCResponseInterceptor()),
                new TransportEventInterceptor(getOnSendEventListener(), getOnReceiveEventListener())
        );
    }

    protected static <T> T createThriftClient(Class<T> clientIface, TProtocol tProtocol, CommonInterceptor interceptor) {
        try {
            Optional<? extends Class> clientClass = Arrays.stream(clientIface.getDeclaringClass().getClasses())
                    .filter(cl -> cl.getSimpleName().equals("Client")).findFirst();
            if (!clientClass.isPresent()) {
                throw new IllegalArgumentException("Client interface doesn't conform to Thrift generated class structure");
            }
            if (!TServiceClient.class.isAssignableFrom(clientClass.get())) {
                throw new IllegalArgumentException("Client class doesn't conform to Thrift generated class structure");
            }
            if (!clientIface.isAssignableFrom(clientClass.get())) {
                throw new IllegalArgumentException("Client class has wrong type which is not assignable to client interface");
            }
            Constructor constructor = clientClass.get().getConstructor(TProtocol.class);
            if (constructor == null) {
                throw new IllegalArgumentException("Client class doesn't have required constructor to be created");
            }
            TServiceClient tClient = (TServiceClient) constructor.newInstance(tProtocol);
            tClient.setInterceptor(interceptor);
            return (T) tClient;
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalArgumentException("Failed to create provider client", e);
        }
    }

    private Runnable createEventRunnable(ClientEventListener eventListener) {
        return () -> eventListener.notifyEvent(new THClientEvent(TraceContext.getCurrentTraceData()));
    }
}
