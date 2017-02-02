package com.rbkmoney.woody.thrift.impl.http;

import com.rbkmoney.woody.api.event.CallType;
import com.rbkmoney.woody.api.event.ClientEventListener;
import com.rbkmoney.woody.api.flow.error.*;
import com.rbkmoney.woody.api.generator.TimestampIdGenerator;
import com.rbkmoney.woody.api.trace.context.TraceContext;
import com.rbkmoney.woody.rpc.Owner;
import com.rbkmoney.woody.rpc.OwnerServiceSrv;
import com.rbkmoney.woody.rpc.test_error;
import com.rbkmoney.woody.thrift.impl.http.event.THClientEvent;
import org.apache.thrift.TException;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

/**
 * Created by vpankrashkin on 06.05.16.
 */
public class TestClientChainEventHandling extends AbstractTest {

    OwnerServiceSrv.Iface handler = new OwnerServiceStub() {
        @Override
        public Owner getOwner(int id) throws TException {
            switch (id) {
                case 0:
                    throw new RuntimeException("err");
                case 10:
                    return client2.getOwner(20);
                case 20:
                    throw new WUndefinedResultException("err");
                case 1:
                    return new Owner(1, "name1");
                default:
                    return new Owner(-1, "default");
            }
        }

        @Override
        public Owner getErrOwner(int id) throws test_error {
            throw new test_error(id);
        }
    };

    OwnerServiceSrv.Iface client1 = createThriftRPCClient(OwnerServiceSrv.Iface.class, new TimestampIdGenerator(), (ClientEventListener<THClientEvent>) (THClientEvent thClientEvent) -> {
        switch (thClientEvent.getEventType()) {
            case ERROR:
                assertFalse(thClientEvent.isSuccessfullCall());
                assertEquals(new Integer(502), thClientEvent.getThriftResponseStatus());
                break;
        }
    });

    OwnerServiceSrv.Iface client2 = createThriftRPCClient(OwnerServiceSrv.Iface.class, new TimestampIdGenerator(), (ClientEventListener<THClientEvent>) (THClientEvent thClientEvent) -> {
        switch (thClientEvent.getEventType()) {
            case ERROR:
                assertFalse(thClientEvent.isSuccessfullCall());
                assertEquals(new Integer(504), thClientEvent.getThriftResponseStatus());
                break;
        }
    });

    @Test
    public void testUndefinedResultError() throws TException {
        addServlet(createMutableTServlet(OwnerServiceSrv.Iface.class, handler), "/");
        try {
            client1.getOwner(10);
            fail();
        } catch (WRuntimeException e) {
            e.printStackTrace();
        }
    }

}