package com.rbkmoney.woody.rpc;

import org.apache.thrift.TException;
import org.apache.thrift.async.AsyncMethodCallback;
import org.apache.thrift.async.TAsyncClientManager;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.server.TNonblockingServer;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.*;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.CountDownLatch;

@Ignore
public class TestSocket {

    private static final int PORT = 7911;

    @BeforeClass
    @SuppressWarnings({"static-access"})
    public static void startServer() throws URISyntaxException, IOException {
        // Start thrift server in a seperate thread
        new Thread(new ServerExample()).start();
        try {
            // wait for the server start up
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testExample() throws TTransportException, TException {
        TTransport transport = new TSocket("localhost", PORT);
        TProtocol protocol = new TBinaryProtocol(transport);
        OwnerServiceSrv.Client client = new OwnerServiceSrv.Client(protocol);
        transport.open();
        Owner bean = client.getOwner(1);
        transport.close();
        Assert.assertEquals("name", bean.getName());
    }

    public static class TestHttpClient {
        public static void main(String[] args) {
            try {
                TTransport transport = new TSocket("localhost", PORT);
                TProtocol protocol = new TBinaryProtocol(transport);
                OwnerServiceSrv.Client client = new OwnerServiceSrv.Client(protocol);
                transport.open();
                Owner bean = client.getOwner(1);
                transport.close();
                System.out.println(bean);
            } catch (TTransportException e) {
                e.printStackTrace();
            } catch (TException e) {
                e.printStackTrace();
            }
        }
    }

    public static class ServerExample implements Runnable {
        public static void main(String[] args) {
            new Thread(new ServerExample()).run();
        }

        @Override
        public void run() {
            try {
                TServerSocket serverTransport = new TServerSocket(PORT);
                OwnerServiceSrv.Processor processor = new OwnerServiceSrv.Processor(new OwnerServiceImpl());
                TServer server = new TThreadPoolServer(new TThreadPoolServer.Args(serverTransport).processor(processor));
                System.out.println("Starting server on port " + PORT);
                server.serve();
            } catch (TTransportException e) {
                e.printStackTrace();
            }
        }
    }

    public static class TestAsyncHttpClient {

        public static void main(String[] args) throws Exception {
            CountDownLatch countDownLatch = new CountDownLatch(1);
            TNonblockingTransport transport = new TNonblockingSocket("localhost", PORT);
            OwnerServiceSrv.AsyncClient client = new OwnerServiceSrv.AsyncClient(new TBinaryProtocol.Factory(), new TAsyncClientManager(), transport);
            client.getOwner(1, new AsyncMethodCallback<Owner>() {
                @Override
                public void onComplete(Owner response) {
                    System.out.println(response);
                    countDownLatch.countDown();
                    transport.close();
                }

                @Override
                public void onError(Exception exception) {
                    exception.printStackTrace();
                }
            });

            countDownLatch.await();
        }
    }

    public static class NonBlockingServerExample implements Runnable {
        public static void main(String[] args) {
            new Thread(new NonBlockingServerExample()).run();
        }

        @Override
        public void run() {
            try {
                TNonblockingServerTransport serverTransport = new TNonblockingServerSocket(PORT, 5000);
                OwnerServiceSrv.AsyncProcessor asyncProcessor = new OwnerServiceSrv.AsyncProcessor(new OwnerServiceAsyncImpl());
                TServer server = new TNonblockingServer(new TNonblockingServer.Args(serverTransport).processor(asyncProcessor));
                System.out.println("Starting non blocking server on port " + PORT);
                server.serve();
            } catch (TTransportException e) {
                e.printStackTrace();
            }
        }
    }

}
