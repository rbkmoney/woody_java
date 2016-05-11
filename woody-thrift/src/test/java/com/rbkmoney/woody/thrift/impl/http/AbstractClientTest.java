package com.rbkmoney.woody.thrift.impl.http;

import com.rbkmoney.woody.api.event.ClientEventListener;
import com.rbkmoney.woody.api.generator.IdGenerator;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.thrift.TException;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.server.TServlet;
import org.apache.thrift.transport.THttpClient;
import org.apache.thrift.transport.TTransportException;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.After;
import org.junit.Before;

import javax.servlet.Servlet;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by vpankrashkin on 06.05.16.
 */
public class AbstractClientTest<I> {
    protected Server server;
    protected Servlet servlet = createMutableTervlet();
    protected int serverPort = 8080;
    protected TProcessor tProcessor;

    @Before
    public void startJetty() throws Exception {

        server = new Server(serverPort);
        ServletContextHandler context = new ServletContextHandler();
        ServletHolder defaultServ = new ServletHolder("default", servlet);
        context.addServlet(defaultServ, "/");
        server.setHandler(context);

        server.start();
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
        return new TServlet(tProcessor, new TCompactProtocol.Factory());
    }

    public TServlet createMutableTervlet() {
        return new TServlet(new TProcessor() {
            @Override
            public boolean process(TProtocol in, TProtocol out) throws TException {
                return tProcessor.process(in, out);
            }
        }, new TCompactProtocol.Factory());
    }

    protected <T> T createThriftClient(Class<T> iface) throws TTransportException {
        try {
            THttpClient thc = new THttpClient(getUrlString(), HttpClientBuilder.create().build());
            TProtocol tProtocol = new TCompactProtocol(thc);
            return THClientBuilder.createThriftClient(iface, tProtocol, null);
        } catch (TTransportException e) {
            throw new RuntimeException(e);
        }
    }


    protected <T> T createThriftRPCClient(Class<T> iface, IdGenerator idGenerator, ClientEventListener eventListener) {
        try {
            THClientBuilder clientBuilder = new THClientBuilder();
            clientBuilder.withAddress(new URI(getUrlString()));
            clientBuilder.withHttpClient(HttpClientBuilder.create().build());
            clientBuilder.withIdGenerator(idGenerator);
            clientBuilder.withEventListener(eventListener);
            return clientBuilder.build(iface);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }


}
