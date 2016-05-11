package com.rbkmoney.woody.rpc;

/**
 * Created by vpankrashkin on 19.04.16.
 */

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


public class TestHttp {

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
    public void testServlet() throws TTransportException, TException {
        String servletUrl = "http://localhost:8080/";
        THttpClient thc = new THttpClient(servletUrl);
        TProtocol loPFactory = new TCompactProtocol(thc);
        OwnerService.Client client = new OwnerService.Client(loPFactory);
        Owner bean = client.getOwner(1);
        Assert.assertEquals("name", bean.getName());

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


}
