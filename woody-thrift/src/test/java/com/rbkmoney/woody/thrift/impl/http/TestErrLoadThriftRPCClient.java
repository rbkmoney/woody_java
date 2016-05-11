package com.rbkmoney.woody.thrift.impl.http;

import com.rbkmoney.woody.api.generator.IdGenerator;
import com.rbkmoney.woody.rpc.Owner;
import com.rbkmoney.woody.rpc.OwnerService;
import com.rbkmoney.woody.rpc.err_one;
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
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.stream.IntStream;

/**
 * Created by vpankrashkin on 05.05.16.
 */
public class TestErrLoadThriftRPCClient {

    private Server server;

    @Before
    public void startJetty() throws Exception {

        server = new Server(8080);
        ServletContextHandler context = new ServletContextHandler();
        ServletHolder defaultServ = new ServletHolder("default", TServletExample.class);
        //defaultServ.setInitParameter("resourceBase",System.getProperty("user.dir"));
        //defaultServ.setInitParameter("dirAllowed","true");
        context.addServlet(defaultServ, "/");
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
        String servletUrl = "http://localhost:8080/";
        OwnerService.Iface tClient = createThriftClient(servletUrl);
        OwnerService.Iface tRPCClient = createThriftRPCClient(servletUrl);

        try {
            tClient.getErrOwner(0);
        } catch (TException e) {
            Assert.assertSame(e.getClass(), err_one.class);
            //e.printStackTrace();
        }

        try {
            tRPCClient.getErrOwner(0);
        } catch (TException e) {
            Assert.assertSame(e.getClass(), err_one.class);
            //e.printStackTrace();
        }

        int testCount = 20000;
        System.out.println("Start warmup");
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
                tClient.getErrOwner(i);
            } catch (err_one e) {

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
                tRPCClient.getErrOwner(i);
            } catch (err_one e) {

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
                            new TestErrLoadThriftRPCClient.OwnerServiceImpl()),
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
        clientBuilder.withIdGenerator(new IdGenerator() {
            @Override
            public String generateId(long timestamp) {
                return Long.toString(timestamp);
            }

            @Override
            public String generateId(long timestamp, int counter) {
                return new StringBuilder().append(timestamp).append(':').append(counter).toString();
            }
        });

        return clientBuilder.build(OwnerService.Iface.class);
    }

    private static class OwnerServiceImpl extends OwnerServiceStub {
        @Override
        public Owner getErrOwner(int id) throws TException, err_one {
            throw new err_one(id);
        }
    }


}
