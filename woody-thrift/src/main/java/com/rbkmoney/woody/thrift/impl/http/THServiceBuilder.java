package com.rbkmoney.woody.thrift.impl.http;

import com.rbkmoney.woody.api.AbstractServiceBuilder;
import com.rbkmoney.woody.api.event.ServiceEventListener;
import com.rbkmoney.woody.api.interceptor.CommonInterceptor;
import com.rbkmoney.woody.api.interceptor.CompositeInterceptor;
import com.rbkmoney.woody.api.interceptor.ContainerCommonInterceptor;
import com.rbkmoney.woody.api.interceptor.ContextInterceptor;
import com.rbkmoney.woody.api.proxy.InstanceMethodCaller;
import com.rbkmoney.woody.api.proxy.MethodCallTracer;
import com.rbkmoney.woody.api.trace.context.EmptyTracer;
import com.rbkmoney.woody.api.trace.context.TraceContext;
import com.rbkmoney.woody.api.transport.TransportEventInterceptor;
import com.rbkmoney.woody.thrift.impl.http.event.THServiceEvent;
import com.rbkmoney.woody.thrift.impl.http.interceptor.*;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.server.TServlet;

import javax.servlet.Servlet;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Optional;

/**
 * Created by vpankrashkin on 28.04.16.
 */
public class THServiceBuilder extends AbstractServiceBuilder<Servlet> {


    @Override
    protected MethodCallTracer getOnCallMetadataExtender(Class serviceInterface) {
        return new EmptyTracer() {
            THErrorMetadataExtender metadataExtender = new THErrorMetadataExtender(serviceInterface);

            @Override
            public void callError(Object[] args, InstanceMethodCaller caller, Throwable error) {
                metadataExtender.extendServiceError(TraceContext.getCurrentTraceData());
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
    protected <T> Servlet createProviderService(Class<T> serviceInterface, T handler) {
        try {
            THErrorMetadataExtender metadataExtender = new THErrorMetadataExtender(serviceInterface);
            TProcessor tProcessor = createThriftProcessor(serviceInterface, handler);
            return createThriftServlet(tProcessor, createTransportInterceptor(metadataExtender), metadataExtender);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected ProxyBuilder createProxyBuilder(Class iface) {
        ProxyBuilder proxyBuilder = super.createProxyBuilder(iface);
        proxyBuilder.setStartEventPhases(ProxyBuilder.EVENT_BEFORE_CALL_START);
        proxyBuilder.setEndEventPhases(ProxyBuilder.EVENT_AFTER_CALL_END);
        proxyBuilder.setErrorEventPhases(ProxyBuilder.EVENT_AFTER_CALL_END);
        return proxyBuilder;
    }

    protected CommonInterceptor createMessageInterceptor() {
        return new CompositeInterceptor(
                new ContainerCommonInterceptor(new THSMessageRequestInterceptor(), new THSMessageResponseInterceptor())
        );
    }

    protected CommonInterceptor createTransportInterceptor(THErrorMetadataExtender metadataExtender) {
        TraceContext traceContext = createTraceContext();
        return new CompositeInterceptor(
                new ContainerCommonInterceptor(null, new THSResponseMetadataInterceptor(metadataExtender)),
                new ContainerCommonInterceptor(new THSRequestInterceptor(), new THSResponseInterceptor(true)),
                new ContextInterceptor(
                        traceContext,
                        new TransportEventInterceptor(getOnReceiveEventListener(), null)
                )
        );
    }

    protected TraceContext createTraceContext() {
        return TraceContext.forServer(() -> {
        }, getOnSendEventListener(), getErrorListener());

    }

    protected TProtocolFactory wrapProtocolFactory(TProtocolFactory tProtocolFactory, CommonInterceptor commonInterceptor) {
        return tTransport -> {
            TProtocol tProtocol = tProtocolFactory.getProtocol(tTransport);
            return new THSProtocolWrapper(tProtocol, commonInterceptor);
        };
    }

    protected Servlet createThriftServlet(TProcessor tProcessor, CommonInterceptor servletInterceptor, THErrorMetadataExtender metadataExtender) {
        CompositeInterceptor protInterceptor = new CompositeInterceptor(
                createMessageInterceptor(),
                new ContainerCommonInterceptor(null, new THSResponseMetadataInterceptor(metadataExtender)),
                new ContainerCommonInterceptor(null, new THSResponseInterceptor(false))
        );
        TProtocolFactory tProtocolFactory = wrapProtocolFactory(new TCompactProtocol.Factory(), protInterceptor);
        return new TServlet(tProcessor, tProtocolFactory, servletInterceptor);
    }

    protected <T> TProcessor createThriftProcessor(Class<T> serviceInterface, T handler) {
        try {
            Optional<? extends Class> processorClass = Arrays.stream(serviceInterface.getDeclaringClass().getClasses())
                    .filter(cl -> cl.getSimpleName().equals("Processor")).findFirst();
            if (!processorClass.isPresent()) {
                throw new IllegalArgumentException("Service interface doesn't conform to Thrift generated class structure");
            }
            if (!TProcessor.class.isAssignableFrom(processorClass.get())) {
                throw new IllegalArgumentException("Service class doesn't conform to Thrift generated class structure");
            }
            Constructor constructor = processorClass.get().getConstructor(serviceInterface);
            if (constructor == null) {
                throw new IllegalArgumentException("Service class doesn't have required constructor to be created");
            }
            TProcessor tProcessor = (TProcessor) constructor.newInstance(handler);
            return tProcessor;
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalArgumentException("Failed to create provider service", e);
        }
    }

    private Runnable createEventRunnable(ServiceEventListener eventListener) {
        return () -> eventListener.notifyEvent(new THServiceEvent(TraceContext.getCurrentTraceData()));
    }
}
