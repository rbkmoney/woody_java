package com.rbkmoney.woody.thrift.impl.http;

import com.rbkmoney.woody.rpc.Owner;
import com.rbkmoney.woody.rpc.OwnerServiceSrv;
import com.rbkmoney.woody.rpc.TestHttp;
import com.rbkmoney.woody.thrift.impl.http.event.ClientEventListenerImpl;
import com.rbkmoney.woody.thrift.impl.http.event.ServiceEventListenerImpl;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.server.TServlet;
import org.apache.thrift.transport.THttpClient;
import org.apache.thrift.transport.TTransportException;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.*;

import javax.servlet.Servlet;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.stream.IntStream;

/**
 * Created by vpankrashkin on 05.05.16.
 */
@Ignore
public class TestLoadThriftRPCClient {

    private Server server;

    @Before
    public void startJetty() throws Exception {

        server = new Server(8080);
        ServletContextHandler context = new ServletContextHandler();
        ServletHolder defaultServ = new ServletHolder("default", TestHttp.TServletExample.class);
        context.addServlet(defaultServ, "/default");

        THServiceBuilder serviceBuilder = new THServiceBuilder();
        serviceBuilder.withEventListener(new ServiceEventListenerImpl());
        Servlet rpcServlet = serviceBuilder.build(OwnerServiceSrv.Iface.class, new OwnerServiceImpl());
        ServletHolder rpcServ = new ServletHolder("rpc", rpcServlet);
        context.addServlet(defaultServ, "/rpc");
        server.setHandler(context);

        // Start Server
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

    @Test
    public void testServlet() throws TTransportException, TException, URISyntaxException {
        String defaultServletUrl = "http://localhost:8080/default";
        String rpcServletUrl = "http://localhost:8080/rpc";
        OwnerServiceSrv.Iface tClient = createThriftClient(defaultServletUrl);
        OwnerServiceSrv.Iface tRPCClient = createThriftRPCClient(rpcServletUrl);

        Owner bean = tClient.getOwner(1);
        Assert.assertEquals("name", bean.getName());
        bean = tRPCClient.getOwner(1);
        Assert.assertEquals("name", bean.getName());

        IntStream.range(1, 1000).forEach(i -> {
            try {
                tClient.getOwner(i);
                tRPCClient.getOwner(i);
            } catch (TException e) {
                e.printStackTrace();
            }
        });
        int testCount = 20000;
        runThriftRPC(testCount, tRPCClient);
        runThrift(testCount, tClient);
        System.out.println("Warmup ended.");
        testCount = 10000;
        runThriftRPC(testCount, tRPCClient);
        runThrift(testCount, tClient);

        testCount = 10000;
        runThrift(testCount, tClient);
        runThriftRPC(testCount, tRPCClient);

    }

    private void runThrift(int testCount, OwnerServiceSrv.Iface tClient) {
        long start = System.currentTimeMillis();
        IntStream.range(1, testCount).forEach(i -> {
            try {
                tClient.getOwner(i);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        System.out.printf("Thrift: %d iterations, %d time\n", testCount, System.currentTimeMillis() - start);

    }

    private void runThriftRPC(int testCount, OwnerServiceSrv.Iface tRPCClient) {
        long start = System.currentTimeMillis();
        IntStream.range(1, testCount).forEach(i -> {
            try {
                tRPCClient.getOwner(i);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        System.out.printf("Thrift RPC: %d iterations, %d time\n", testCount, System.currentTimeMillis() - start);
    }

    public static class TServletExample extends TServlet {
        public TServletExample() {
            super(
                    new OwnerServiceSrv.Processor(
                            new OwnerServiceImpl()),
                    new TBinaryProtocol.Factory()
            );
        }
    }

    private OwnerServiceSrv.Iface createThriftClient(String url) throws TTransportException {
        THttpClient thc = new THttpClient(url, HttpClients.createMinimal());
        TProtocol loPFactory = new TBinaryProtocol(thc);
        return new OwnerServiceSrv.Client(loPFactory);
    }

    private OwnerServiceSrv.Iface createThriftRPCClient(String url) throws URISyntaxException {
        THClientBuilder clientBuilder = new THClientBuilder();
        clientBuilder.withAddress(new URI(url));
        clientBuilder.withHttpClient(HttpClients.createMinimal());
        clientBuilder.withEventListener(new ClientEventListenerImpl());

        return clientBuilder.build(OwnerServiceSrv.Iface.class);
    }

    private static class OwnerServiceImpl extends OwnerServiceStub {
        @Override
        public Owner getOwner(int id) throws TException {
            return new Owner(id, "name");
        }
    }


}
