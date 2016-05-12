package com.rbkmoney.woody.thrift.impl.http;

import com.rbkmoney.woody.rpc.Owner;
import com.rbkmoney.woody.rpc.OwnerService;
import com.rbkmoney.woody.rpc.err_one;
import com.rbkmoney.woody.thrift.impl.http.event.ClientEventListenerImpl;
import com.rbkmoney.woody.thrift.impl.http.event.ClientEventLogListener;
import com.rbkmoney.woody.thrift.impl.http.event.ServiceEventListenerImpl;
import com.rbkmoney.woody.thrift.impl.http.event.ServiceEventLogListener;
import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.servlet.Servlet;

import static java.lang.System.out;

/**
 * Created by vpankrashkin on 12.05.16.
 */
@Ignore
public class TestChildRequests extends AbstractTest {

    ClientEventListenerImpl clientEventListener = new ClientEventListenerImpl();
    ServiceEventListenerImpl serviceEventListener = new ServiceEventListenerImpl();

    OwnerService.Iface client1 = createThriftRPCClient(OwnerService.Iface.class, new IdGeneratorStub(), new ClientEventLogListener(), getUrlString("/rpc"));
    OwnerService.Iface client2 = createThriftRPCClient(OwnerService.Iface.class, new IdGeneratorStub(), new ClientEventLogListener(), getUrlString("/rpc"));
    OwnerService.Iface handler = new OwnerServiceStub() {
        @Override
        public Owner getErrOwner(int id) throws TException, err_one {
            switch (id) {
                case 0:
                    Owner owner = client2.getOwner(0);
                    client2.setOwnerOneway(owner);
                    return client2.getOwner(10);
                case 200:
                    throw new err_one(200);
                case 500:
                    throw new RuntimeException("Test");
                default:
                    return super.getErrOwner(id);
            }
        }
    };

    Servlet servlet = createThrftRPCService(OwnerService.Iface.class, handler, new IdGeneratorStub(), new ServiceEventLogListener());

    @Before
    public void before() {
        addServlet(servlet, "/rpc");
    }

    @Test
    public void testEventOrder() throws TException {
        out.println("Root call>");
        Assert.assertEquals(new Owner(10, "10"), client1.getErrOwner(0));
        out.println("<");

        out.println("Root call>");
        try {
            client1.getErrOwner(200);
            Assert.fail();
        } catch (err_one e) {
        }
        out.println("<");
        out.println("Root call>");
        try {
            client1.getErrOwner(500);
        } catch (TTransportException e) {
        }
        out.println("<");


    }

}
