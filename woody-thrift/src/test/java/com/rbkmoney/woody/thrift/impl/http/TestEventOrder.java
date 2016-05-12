package com.rbkmoney.woody.thrift.impl.http;

import com.rbkmoney.woody.rpc.Owner;
import com.rbkmoney.woody.rpc.OwnerService;
import com.rbkmoney.woody.rpc.err_one;
import com.rbkmoney.woody.thrift.impl.http.event.ClientActionListener;
import com.rbkmoney.woody.thrift.impl.http.event.ClientEventListenerImpl;
import com.rbkmoney.woody.thrift.impl.http.event.ServiceActionListener;
import com.rbkmoney.woody.thrift.impl.http.event.ServiceEventListenerImpl;
import org.apache.thrift.TException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.Servlet;

import static org.easymock.EasyMock.*;

/**
 * Created by vpankrashkin on 12.05.16.
 */
public class TestEventOrder extends AbstractTest {

    ClientEventListenerImpl clientEventListener = new ClientEventListenerImpl();
    ServiceEventListenerImpl serviceEventListener = new ServiceEventListenerImpl();
    OwnerService.Iface handler = new OwnerServiceStub() {
        @Override
        public Owner getErrOwner(int id) throws TException, err_one {
            switch (id) {
                case 500:
                    throw new RuntimeException("Test");
                default:
                    return super.getErrOwner(id);
            }
        }
    };

    Servlet servlet = createThrftRPCService(OwnerService.Iface.class, handler, new IdGeneratorStub(), serviceEventListener);

    OwnerService.Iface client = createThriftRPCClient(OwnerService.Iface.class, new IdGeneratorStub(), clientEventListener, getUrlString("/rpc"));

    @Before
    public void before() {
        addServlet(servlet, "/rpc");
    }

    @Test
    public void testEventOrder() throws TException {

        ClientActionListener clientActionListener = createStrictMock(ClientActionListener.class);
        expect(clientActionListener.callService(anyObject())).andReturn(null);
        expect(clientActionListener.clientSend(anyObject())).andReturn(null);
        expect(clientActionListener.clientReceive(anyObject())).andReturn(null);
        expect(clientActionListener.serviceResult(anyObject())).andReturn(null);
        replay(clientActionListener);

        ServiceActionListener serviceEventActionListener = createStrictMock(ServiceActionListener.class);
        expect(serviceEventActionListener.serviceReceive(anyObject())).andReturn(null);
        expect(serviceEventActionListener.callHandler(anyObject())).andReturn(null);
        expect(serviceEventActionListener.handlerResult(anyObject())).andReturn(null);
        expect(serviceEventActionListener.serviceResult(anyObject())).andReturn(null);
        replay(serviceEventActionListener);

        clientEventListener.setEventActionListener(clientActionListener);
        serviceEventListener.setEventActionListener(serviceEventActionListener);

        client.getOwner(0);

        verify(clientActionListener);
    }

    @Test
    public void testOneWayEventOrder() throws TException {

        ClientActionListener clientActionListener = createStrictMock(ClientActionListener.class);
        expect(clientActionListener.callService(anyObject())).andReturn(null);
        expect(clientActionListener.clientSend(anyObject())).andReturn(null);
        expect(clientActionListener.clientReceive(anyObject())).andReturn(null);
        expect(clientActionListener.serviceResult(anyObject())).andReturn(null);
        replay(clientActionListener);

        ServiceActionListener serviceEventActionListener = createStrictMock(ServiceActionListener.class);
        expect(serviceEventActionListener.serviceReceive(anyObject())).andReturn(null);
        expect(serviceEventActionListener.callHandler(anyObject())).andReturn(null);
        expect(serviceEventActionListener.handlerResult(anyObject())).andReturn(null);
        expect(serviceEventActionListener.serviceResult(anyObject())).andReturn(null);
        replay(serviceEventActionListener);

        clientEventListener.setEventActionListener(clientActionListener);
        serviceEventListener.setEventActionListener(serviceEventActionListener);

        client.setOwnerOneway(new Owner(0, ""));

        verify(clientActionListener);
    }

    @Test
    public void testKnownErrEventOrder() throws TException {

        ClientActionListener clientActionListener = createStrictMock(ClientActionListener.class);
        expect(clientActionListener.callService(anyObject())).andReturn(null);
        expect(clientActionListener.clientSend(anyObject())).andReturn(null);
        expect(clientActionListener.clientReceive(anyObject())).andReturn(null);
        expect(clientActionListener.error(anyObject())).andReturn(null);
        replay(clientActionListener);

        ServiceActionListener serviceEventActionListener = createStrictMock(ServiceActionListener.class);
        expect(serviceEventActionListener.serviceReceive(anyObject())).andReturn(null);
        expect(serviceEventActionListener.callHandler(anyObject())).andReturn(null);
        expect(serviceEventActionListener.error(anyObject())).andReturn(null);
        expect(serviceEventActionListener.serviceResult(anyObject())).andReturn(null);
        replay(serviceEventActionListener);

        clientEventListener.setEventActionListener(clientActionListener);
        serviceEventListener.setEventActionListener(serviceEventActionListener);

        try {
            client.getErrOwner(1);
            Assert.fail("Exception should be here");
        } catch (err_one e) {
            Assert.assertEquals(1, e.getId());
        }

        verify(clientActionListener);
    }

    @Test
    public void testUnknownErrEventOrder() throws TException {

        ClientActionListener clientActionListener = createStrictMock(ClientActionListener.class);
        expect(clientActionListener.callService(anyObject())).andReturn(null);
        expect(clientActionListener.clientSend(anyObject())).andReturn(null);
        expect(clientActionListener.clientReceive(anyObject())).andReturn(null);
        expect(clientActionListener.error(anyObject())).andReturn(null);
        replay(clientActionListener);

        ServiceActionListener serviceEventActionListener = createStrictMock(ServiceActionListener.class);
        expect(serviceEventActionListener.serviceReceive(anyObject())).andReturn(null);
        expect(serviceEventActionListener.callHandler(anyObject())).andReturn(null);
        expect(serviceEventActionListener.error(anyObject())).andReturn(null);
        expect(serviceEventActionListener.serviceResult(anyObject())).andReturn(null);
        replay(serviceEventActionListener);

        clientEventListener.setEventActionListener(clientActionListener);
        serviceEventListener.setEventActionListener(serviceEventActionListener);

        try {
            client.getErrOwner(500);
            Assert.fail("Exception should be here");
        } catch (Exception e) {
            e.printStackTrace();
        }

        verify(clientActionListener);
    }
}
