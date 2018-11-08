package com.rbkmoney.woody.thrift.impl.http;

import com.rbkmoney.woody.api.event.ClientEventListener;
import com.rbkmoney.woody.api.event.ServiceEventListener;
import com.rbkmoney.woody.api.generator.IdGenerator;
import com.rbkmoney.woody.api.trace.context.metadata.MetadataExtensionKit;
import com.rbkmoney.woody.rpc.OwnerServiceSrv;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.thrift.TException;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.server.TServlet;
import org.apache.thrift.transport.THttpClient;
import org.apache.thrift.transport.TIOStreamTransport;
import org.apache.thrift.transport.TTransportException;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.After;
import org.junit.Before;

import javax.servlet.Servlet;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Created by vpankrashkin on 06.05.16.
 */
public class AbstractTest {
    public static final int networkTimeout = 5000;
    private HandlerCollection handlerCollection;
    protected Server server;
    protected int serverPort = 8080;
    protected TProcessor tProcessor;

    @Before
    public void startJetty() throws Exception {

        server = new Server(serverPort);
        HandlerCollection contextHandlerCollection = new HandlerCollection(true); // important! use parameter
        // mutableWhenRunning==true
        this.handlerCollection = contextHandlerCollection;
        server.setHandler(contextHandlerCollection);

        server.start();
    }

    protected void addServlet(Servlet servlet, String mapping) {
        try {
            ServletContextHandler context = new ServletContextHandler();
            ServletHolder defaultServ = new ServletHolder(mapping, servlet);
            context.addServlet(defaultServ, mapping);
            handlerCollection.addHandler(context);
            context.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected void addServlets(Map.Entry<String, Servlet>... mapping) {

        try {
            Arrays.stream(mapping).map(entry -> {
                ServletContextHandler context = new ServletContextHandler();
                ServletHolder defaultServ = new ServletHolder(entry.getKey(), entry.getValue());
                context.addServlet(defaultServ, entry.getKey());
                handlerCollection.addHandler(context);
                return context;
            }).sorted(Comparator.comparingInt(o -> o.hashCode())).forEach(context -> {
                try {
                    context.start();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @After
    public void stopJetty() {
        try {
            server.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    protected String getUrlString() {
        return "http://localhost:" + serverPort;
    }

    public TServlet createTServlet(TProcessor tProcessor) {
        return new TServlet(tProcessor, new TBinaryProtocol.Factory());
    }

    public <T> Servlet createMutableTServlet(Class<T> type, T handler) {
        return new THServiceBuilder().build(type, handler);
    }

    protected <T> Servlet createThriftRPCService(Class<T> iface, T handler) {
        return createThriftRPCService(iface, handler, null);
    }

    protected <T> Servlet createThriftRPCService(Class<T> iface, T handler, ServiceEventListener eventListener) {
        return createThriftRPCService(iface, handler, eventListener, null);
    }

    protected <T> Servlet createThriftRPCService(Class<T> iface, T handler, ServiceEventListener eventListener, List<MetadataExtensionKit> extensionKits) {
        THServiceBuilder serviceBuilder = new THServiceBuilder();
        if (eventListener != null) {
            serviceBuilder.withEventListener(eventListener);
        }
        serviceBuilder.withMetaExtensions(extensionKits);
        return serviceBuilder.build(iface, handler);
    }

    protected String getUrlString(String contextPath) {
        return getUrlString() + contextPath;
    }

    protected <T> T createThriftClient(Class<T> iface) throws TTransportException {
        return createThriftRPCClient(iface, getUrlString());
    }

    protected <T> T createThriftClient(Class<T> iface, String url) throws TTransportException {
        try {
            THttpClient thc = new THttpClient(url, HttpClients.createMinimal());
            TProtocol tProtocol = new TBinaryProtocol(thc);
            return THClientBuilder.createThriftClient(iface, tProtocol);
        } catch (TTransportException e) {
            throw new RuntimeException(e);
        }
    }

    protected <T> T createThriftRPCClient(Class<T> iface, String url) {
        return createThriftRPCClient(iface, null, null, url);
    }

    protected <T> T createThriftRPCClient(Class<T> iface, List<MetadataExtensionKit> extensionKits, String url) {
        return createThriftRPCClient(iface, null, null, extensionKits, url);
    }

    protected <T> T createThriftRPCClient(Class<T> iface, IdGenerator idGenerator, ClientEventListener eventListener) {
        return createThriftRPCClient(iface, idGenerator, eventListener, getUrlString());
    }

    protected <T> T createThriftRPCClient(Class<T> iface, IdGenerator idGenerator, ClientEventListener eventListener, String url) {
        return createThriftRPCClient(iface, idGenerator, eventListener, null, url);
    }

    protected <T> T createThriftRPCClient(Class<T> iface, IdGenerator idGenerator, ClientEventListener eventListener, List<MetadataExtensionKit> extensionKits, String url) {
        return createThriftRPCClient(iface, idGenerator, eventListener, extensionKits, url, networkTimeout);
    }

    protected <T> T createThriftRPCClient(Class<T> iface, IdGenerator idGenerator, ClientEventListener eventListener, int timeout) {
        return createThriftRPCClient(iface, idGenerator, eventListener, null, getUrlString(), timeout);
    }

    protected <T> T createThriftRPCClient(Class<T> iface, IdGenerator idGenerator, ClientEventListener eventListener, String url, int timeout) {
        return createThriftRPCClient(iface, idGenerator, eventListener, null, url, timeout);
    }

    protected <T> T createThriftRPCClient(Class<T> iface, IdGenerator idGenerator, ClientEventListener eventListener, List<MetadataExtensionKit> extensionKits, String url, int timeout) {
        return createThriftRPCClient(iface, idGenerator, eventListener, extensionKits, url, timeout, null);
    }

    protected <T> T createThriftRPCClient(Class<T> iface, String url, HttpClient httpClient) {
        return createThriftRPCClient(iface, url, networkTimeout, httpClient);
    }

    protected <T> T createThriftRPCClient(Class<T> iface, String url, int timeout) {
        return createThriftRPCClient(iface, url, timeout, null);
    }

    protected <T> T createThriftRPCClient(Class<T> iface, String url, int timeout, HttpClient httpClient) {
        return createThriftRPCClient(iface, null, null, null, url, timeout, httpClient);
    }

    protected <T> T createThriftRPCClient(Class<T> iface, IdGenerator idGenerator, ClientEventListener eventListener, List<MetadataExtensionKit> extensionKits, String url, int timeout, HttpClient httpClient) {
        try {
            //todo fix loosing log events for THClientBuilder
            THSpawnClientBuilder clientBuilder = new THSpawnClientBuilder();
            clientBuilder.withNetworkTimeout(timeout);
            clientBuilder.withAddress(new URI(url));
            clientBuilder.withHttpClient(httpClient);
            if (idGenerator != null) {
                clientBuilder.withIdGenerator(idGenerator);
            }
            if (eventListener != null) {
                clientBuilder.withEventListener(eventListener);
            }
            clientBuilder.withMetaExtensions(extensionKits);
            return clientBuilder.build(iface);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    protected void writeResultMessage(ServletRequest servletRequest, ServletResponse servletResponse) throws IOException {
        TBinaryProtocol tBinaryProtocol = new TBinaryProtocol(
                new TIOStreamTransport(servletRequest.getInputStream(), servletResponse.getOutputStream())
        );
        try {
            tBinaryProtocol.writeMessageBegin(tBinaryProtocol.readMessageBegin());
            OwnerServiceSrv.getIntValue_result intValueResult = new OwnerServiceSrv.getIntValue_result();
            intValueResult.setSuccess(42);
            intValueResult.write(tBinaryProtocol);
            tBinaryProtocol.writeMessageEnd();
            tBinaryProtocol.getTransport().flush();
        } catch (TException ex) {
            throw new RuntimeException(ex);
        }
    }
}
