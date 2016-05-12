package com.rbkmoney.woody.thrift.impl.http;

import com.rbkmoney.woody.api.event.ClientEvent;
import com.rbkmoney.woody.api.event.ClientEventListener;
import com.rbkmoney.woody.api.event.ServiceEvent;
import com.rbkmoney.woody.api.event.ServiceEventListener;
import com.rbkmoney.woody.rpc.Owner;
import com.rbkmoney.woody.rpc.OwnerService;
import com.rbkmoney.woody.rpc.TestHttp;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TCompactProtocol;
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
        serviceBuilder.withIdGenerator(new IdGeneratorStub());
        serviceBuilder.withEventListener(new ServiceEventListener() {
            @Override
            public void notifyEvent(ServiceEvent event) {

            }
        });
        Servlet rpcServlet = serviceBuilder.build(OwnerService.Iface.class, new OwnerServiceImpl());
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
        OwnerService.Iface tClient = createThriftClient(defaultServletUrl);
        OwnerService.Iface tRPCClient = createThriftRPCClient(rpcServletUrl);

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
        runHtriftRPC(testCount, tRPCClient);
        runThrift(testCount, tClient);
        System.out.println("Warmup ended.");
        testCount = 10000;
        runHtriftRPC(testCount, tRPCClient);
        runThrift(testCount, tClient);

        testCount = 10000;
        runThrift(testCount, tClient);
        runHtriftRPC(testCount, tRPCClient);

    }

    private void runThrift(int testCount, OwnerService.Iface tClient) {
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

    private void runHtriftRPC(int testCount, OwnerService.Iface tRPCClient) {
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
                    new OwnerService.Processor(
                            new OwnerServiceImpl()),
                    new TCompactProtocol.Factory()
            );
        }
    }

    private OwnerService.Iface createThriftClient(String url) throws TTransportException {
        THttpClient thc = new THttpClient(url, HttpClientBuilder.create().build());
        TProtocol loPFactory = new TCompactProtocol(thc);
        return new OwnerService.Client(loPFactory);
    }

    private OwnerService.Iface createThriftRPCClient(String url) throws URISyntaxException {
        THClientBuilder clientBuilder = new THClientBuilder();
        clientBuilder.withAddress(new URI(url));
        clientBuilder.withHttpClient(HttpClientBuilder.create().build());
        clientBuilder.withIdGenerator(new IdGeneratorStub());
        clientBuilder.withEventListener(new ClientEventListener() {
            @Override
            public void notifyEvent(ClientEvent event) {

            }
        });

        return clientBuilder.build(OwnerService.Iface.class);
    }

    private static class OwnerServiceImpl extends OwnerServiceStub {
        @Override
        public Owner getOwner(int id) throws TException {
            return new Owner(id, "name");
        }
    }


}
