package com.rbkmoney.woody.thrift.impl.http;

import com.rbkmoney.woody.api.AbstractServiceBuilder;
import com.rbkmoney.woody.api.event.ServiceEventListener;
import com.rbkmoney.woody.api.flow.error.WErrorDefinition;
import com.rbkmoney.woody.api.interceptor.*;
import com.rbkmoney.woody.api.provider.ProviderEventInterceptor;
import com.rbkmoney.woody.api.trace.ContextSpan;
import com.rbkmoney.woody.api.trace.context.TraceContext;
import com.rbkmoney.woody.api.transport.TransportEventInterceptor;
import com.rbkmoney.woody.thrift.impl.http.error.THErrorMapProcessor;
import com.rbkmoney.woody.thrift.impl.http.event.THServiceEvent;
import com.rbkmoney.woody.thrift.impl.http.interceptor.THMessageInterceptor;
import com.rbkmoney.woody.thrift.impl.http.interceptor.THTransportInterceptor;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.server.TServlet;

import javax.servlet.Servlet;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

/**
 * Created by vpankrashkin on 28.04.16.
 */
public class THServiceBuilder extends AbstractServiceBuilder<Servlet> {

    @Override
    public THServiceBuilder withEventListener(ServiceEventListener listener) {
        return (THServiceBuilder) super.withEventListener(listener);
    }

    protected BiConsumer<WErrorDefinition, ContextSpan> getErrorDefinitionConsumer() {
        return (eDef, contextSpan) ->
                contextSpan.getMetadata().removeValue(THMetadataProperties.TH_TRANSPORT_RESPONSE_SET_FLAG);
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
            THErrorMapProcessor errorMapProcessor = THErrorMapProcessor.getInstance(false, serviceInterface);
            TProcessor tProcessor = createThriftProcessor(serviceInterface, handler);
            return createThriftServlet(tProcessor, createInterceptor(errorMapProcessor, true), errorMapProcessor);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected TProtocolFactory createTransferProtocolFactory() {
        return new TBinaryProtocol.Factory();
    }

    /**
     * @param isTransportLevel true - if this interceptor must be invoked on the lowers level (transport), next is thrift protocol level wit allows message read/write detection
     * */
    protected CommonInterceptor createInterceptor(THErrorMapProcessor errorMapProcessor, boolean isTransportLevel) {
        List<CommonInterceptor> interceptors = new ArrayList<>();

        if (!isTransportLevel) {
            interceptors.add(new ContainerCommonInterceptor(
                    new THMessageInterceptor(false, true), new THMessageInterceptor(false, false)
            ));
        }

        interceptors.add(new ErrorMappingInterceptor(errorMapProcessor, getErrorDefinitionConsumer()));
        interceptors.add(new ContainerCommonInterceptor(
                isTransportLevel ? new THTransportInterceptor(false, true) : null,
                new THTransportInterceptor(false, false)
        ));

        if (isTransportLevel) {
            interceptors.add(new ProviderEventInterceptor(getOnSendEventListener(), null));
            interceptors.add(new ContextInterceptor(
                    TraceContext.forService(),
                    new TransportEventInterceptor(getOnReceiveEventListener(), null, getErrorListener())
            ));
        }
        return new CompositeInterceptor(interceptors.toArray(new CommonInterceptor[interceptors.size()]));

    }

    protected Servlet createThriftServlet(TProcessor tProcessor, CommonInterceptor servletInterceptor, THErrorMapProcessor errorMapProcessor) {
        TProtocolFactory tProtocolFactory = createTransferProtocolFactory();
        BuilderUtils.wrapProtocolFactory(tProtocolFactory, createInterceptor(errorMapProcessor, false));

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
            throw new IllegalArgumentException("Failed to createCtxBundle provider service", e);
        }
    }

    private Runnable createEventRunnable(ServiceEventListener eventListener) {
        return () -> eventListener.notifyEvent(new THServiceEvent(TraceContext.getCurrentTraceData()));
    }
}
