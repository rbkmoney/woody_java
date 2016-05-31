package com.rbkmoney.woody.thrift.impl.http;

import com.rbkmoney.woody.rpc.Owner;
import com.rbkmoney.woody.rpc.OwnerServiceSrv;
import com.rbkmoney.woody.rpc.test_error;
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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.stream.IntStream;

/**
 * Created by vpankrashkin on 05.05.16.
 */
@Ignore
public class TestLoadErrThriftRPCClient {

    private Server server;

    @Before
    public void startJetty() throws Exception {

        server = new Server(8080);
        ServletContextHandler context = new ServletContextHandler();
        ServletHolder defaultServ = new ServletHolder("default", TServletExample.class);
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
        OwnerServiceSrv.Iface tClient = createThriftClient(servletUrl);
        OwnerServiceSrv.Iface tRPCClient = createThriftRPCClient(servletUrl);

        try {
            tClient.getErrOwner(0);
        } catch (TException e) {
            Assert.assertSame(e.getClass(), test_error.class);
            //e.printStackTrace();
        }

        try {
            tRPCClient.getErrOwner(0);
        } catch (TException e) {
            Assert.assertSame(e.getClass(), test_error.class);
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

    private void runThrift(int testCount, OwnerServiceSrv.Iface tClient) {
        long start = System.currentTimeMillis();
        IntStream.range(1, testCount).forEach(i -> {
            try {
                tClient.getErrOwner(i);
            } catch (test_error e) {

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        System.out.printf("Thrift: %d iterations, %d time\n", testCount, System.currentTimeMillis() - start);

    }

    private void runHtriftRPC(int testCount, OwnerServiceSrv.Iface tRPCClient) {
        long start = System.currentTimeMillis();
        IntStream.range(1, testCount).forEach(i -> {
            try {
                tRPCClient.getErrOwner(i);
            } catch (test_error e) {

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
                            new TestLoadErrThriftRPCClient.OwnerServiceImpl()),
                    new TCompactProtocol.Factory()
            );
        }
    }

    private OwnerServiceSrv.Iface createThriftClient(String url) throws TTransportException {
        THttpClient thc = new THttpClient(url, HttpClientBuilder.create().build());
        TProtocol loPFactory = new TCompactProtocol(thc);
        return new OwnerServiceSrv.Client(loPFactory);
    }

    private OwnerServiceSrv.Iface createThriftRPCClient(String url) throws URISyntaxException {
        THClientBuilder clientBuilder = new THClientBuilder();
        clientBuilder.withAddress(new URI(url));
        clientBuilder.withHttpClient(HttpClientBuilder.create().build());

        return clientBuilder.build(OwnerServiceSrv.Iface.class);
    }

    private static class OwnerServiceImpl extends OwnerServiceStub {
        @Override
        public Owner getErrOwner(int id) throws TException, test_error {
            throw new test_error(id);
        }
    }


}
