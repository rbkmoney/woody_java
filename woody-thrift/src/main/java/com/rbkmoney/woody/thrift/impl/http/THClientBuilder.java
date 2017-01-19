package com.rbkmoney.woody.thrift.impl.http;

import com.rbkmoney.woody.api.AbstractClientBuilder;
import com.rbkmoney.woody.api.event.ClientEventListener;
import com.rbkmoney.woody.api.flow.WFlow;
import com.rbkmoney.woody.api.flow.error.ErrorMapProcessor;
import com.rbkmoney.woody.api.flow.error.WErrorDefinition;
import com.rbkmoney.woody.api.flow.error.WErrorMapper;
import com.rbkmoney.woody.api.flow.error.WErrorType;
import com.rbkmoney.woody.api.generator.IdGenerator;
import com.rbkmoney.woody.api.interceptor.CommonInterceptor;
import com.rbkmoney.woody.api.interceptor.CompositeInterceptor;
import com.rbkmoney.woody.api.interceptor.ContainerCommonInterceptor;
import com.rbkmoney.woody.api.provider.ProviderEventInterceptor;
import com.rbkmoney.woody.api.trace.ContextSpan;
import com.rbkmoney.woody.api.trace.MetadataProperties;
import com.rbkmoney.woody.api.trace.context.TraceContext;
import com.rbkmoney.woody.api.transport.TransportEventInterceptor;
import com.rbkmoney.woody.thrift.impl.http.error.THErrorMapProcessor;
import com.rbkmoney.woody.thrift.impl.http.event.THClientEvent;
import com.rbkmoney.woody.thrift.impl.http.interceptor.THMessageInterceptor;
import com.rbkmoney.woody.thrift.impl.http.interceptor.THTransportInterceptor;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.thrift.TServiceClient;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.transport.THttpClient;
import org.apache.thrift.transport.TTransport;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.BiConsumer;

/**
 * Created by vpankrashkin on 28.04.16.
 */
public class THClientBuilder extends AbstractClientBuilder {

    private HttpClient httpClient;
    private WErrorMapper customErrMapper;

    public THClientBuilder() {
        this.httpClient = createHttpClient();
        super.withIdGenerator(WFlow.createDefaultIdGenerator());
    }

    public THClientBuilder withErrorMapper(WErrorMapper errorMapper) {
        customErrMapper = errorMapper;
        return this;
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
    protected BiConsumer<WErrorDefinition, ContextSpan> getErrorDefinitionConsumer() {
        return (eDef, contextSpan) -> {};
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
    protected ErrorMapProcessor createErrorMapProcessor(Class iface) {
        return THErrorMapProcessor.getInstance(true, iface, customErrMapper);
    }

    @Override
    protected <T> T createProviderClient(Class<T> iface) {
        try {
            THttpClient tHttpClient = new THttpClient(getAddress().toString(), getHttpClient(), createTransportInterceptor());
            TProtocol tProtocol = createProtocol(tHttpClient);
            return createThriftClient(iface, tProtocol);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected TProtocolFactory createTransferProtocolFactory() {
        return new TBinaryProtocol.Factory();
    }

    protected TProtocol createProtocol(TTransport tTransport) {
        return BuilderUtils.wrapProtocolFactory(createTransferProtocolFactory(), createMessageInterceptor(), true).getProtocol(tTransport);
    }

    protected HttpClient createHttpClient() {
        return HttpClientBuilder.create().build();
    }

    protected CommonInterceptor createMessageInterceptor() {
        return new CompositeInterceptor(
                new ContainerCommonInterceptor(new THMessageInterceptor(true, true), new THMessageInterceptor(true, false)),
                new ProviderEventInterceptor(getOnCallStartEventListener(), null)
        );
    }

    protected CommonInterceptor createTransportInterceptor() {
        return new CompositeInterceptor(
                new ContainerCommonInterceptor(new THTransportInterceptor(true, true), new THTransportInterceptor(true, false)),
                new TransportEventInterceptor(getOnSendEventListener(), getOnReceiveEventListener(), null)
        );
    }

    protected static <T> T createThriftClient(Class<T> clientIface, TProtocol tProtocol) {
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
            return (T) tClient;
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalArgumentException("Failed to createCtxBundle provider client", e);
        }
    }

    private Runnable createEventRunnable(ClientEventListener eventListener) {
        return () -> eventListener.notifyEvent(new THClientEvent(TraceContext.getCurrentTraceData()));
    }
}
