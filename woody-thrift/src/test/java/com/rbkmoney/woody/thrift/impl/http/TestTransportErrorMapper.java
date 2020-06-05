package com.rbkmoney.woody.thrift.impl.http;

import com.rbkmoney.woody.api.event.ClientEventListener;
import com.rbkmoney.woody.api.flow.error.WErrorSource;
import com.rbkmoney.woody.api.flow.error.WErrorType;
import com.rbkmoney.woody.api.flow.error.WRuntimeException;
import com.rbkmoney.woody.api.generator.TimestampIdGenerator;
import com.rbkmoney.woody.api.trace.ContextUtils;
import com.rbkmoney.woody.rpc.Owner;
import com.rbkmoney.woody.rpc.OwnerServiceSrv;
import com.rbkmoney.woody.rpc.test_error;
import com.rbkmoney.woody.thrift.impl.http.event.THClientEvent;
import org.apache.thrift.TException;
import org.junit.Test;

import java.net.UnknownHostException;
import java.util.concurrent.Semaphore;

import static org.junit.Assert.*;

public class TestTransportErrorMapper extends AbstractTest {
    private final Semaphore semaphore = new Semaphore(0);

    OwnerServiceSrv.Iface handler = new OwnerServiceStub() {
        @Override
        public Owner getOwner(int id) throws TException {
            switch (id) {
                case 0:
                    return timeoutClient.getOwner(1);
                case 1:
                    try {
                        Thread.sleep(networkTimeout * 2);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    throw new RuntimeException();
                case 2:
                    try {
                        semaphore.release(1);
                        Thread.sleep(networkTimeout * 2);
                        return new Owner(2, "name");
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
            }
            return null;
        }

        @Override
        public Owner getErrOwner(int id) throws test_error {
            throw new test_error(id);
        }
    };

    OwnerServiceSrv.Iface noTimeoutClient = createThriftRPCClient(OwnerServiceSrv.Iface.class, new TimestampIdGenerator(), (ClientEventListener<THClientEvent>) (THClientEvent thClientEvent) -> {
        switch (thClientEvent.getEventType()) {
            case ERROR:
                if (thClientEvent.getCallArguments()[0].equals(2)) {
                    //do nothing, response is already closed
                } else {
                    assertFalse(thClientEvent.isSuccessfulCall());
                    assertEquals(new Integer(502), thClientEvent.getThriftResponseStatus());
                    assertEquals(WErrorType.UNDEFINED_RESULT, thClientEvent.getErrorDefinition().getErrorType());
                }
                break;
        }
    }, -1);

    OwnerServiceSrv.Iface timeoutClient = createThriftRPCClient(OwnerServiceSrv.Iface.class, new TimestampIdGenerator(), (ClientEventListener<THClientEvent>) (THClientEvent thClientEvent) -> {
        switch (thClientEvent.getEventType()) {
            case ERROR:
                assertFalse(thClientEvent.isSuccessfulCall());
                assertEquals(WErrorType.UNDEFINED_RESULT, thClientEvent.getErrorDefinition().getErrorType());
                break;
        }
    });

    OwnerServiceSrv.Iface unknownHostClient = createThriftRPCClient(OwnerServiceSrv.Iface.class, new TimestampIdGenerator(), (ClientEventListener<THClientEvent>) (THClientEvent thClientEvent) -> {
        switch (thClientEvent.getEventType()) {
            case ERROR:
                assertFalse(thClientEvent.isSuccessfulCall());
                assertEquals(WErrorType.UNAVAILABLE_RESULT, thClientEvent.getErrorDefinition().getErrorType());
                break;
        }
    }, "http://wronghost:" + serverPort);

    @Test
    public void testSocketTimeoutError() throws TException {
        //Socket timeout expected
        addServlet(createMutableTServlet(OwnerServiceSrv.Iface.class, handler), "/");
        try {
            noTimeoutClient.getOwner(0);
            fail();
        } catch (WRuntimeException e) {
            assertEquals("Network timeout expected", WErrorType.UNDEFINED_RESULT, e.getErrorDefinition().getErrorType());
            assertEquals("Error returned from child request", WErrorSource.EXTERNAL, e.getErrorDefinition().getErrorSource());
            assertEquals("Generation source is external for all network errors", WErrorSource.EXTERNAL, e.getErrorDefinition().getGenerationSource());
        }
    }

    @Test
    public void testNoHttpResponseError() throws TException, InterruptedException {
        addServlet(createMutableTServlet(OwnerServiceSrv.Iface.class, handler), "/");
        Thread t = null;
        try {
            t = new Thread(() -> {
                try {
                    semaphore.acquire(1);
                    server.stop();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            t.setDaemon(true);
            t.start();
            noTimeoutClient.getOwner(2);
            fail();
        } catch (WRuntimeException e) {
            assertEquals(WErrorType.UNAVAILABLE_RESULT, e.getErrorDefinition().getErrorType());
            assertEquals("Error returned on main client call", WErrorSource.INTERNAL, e.getErrorDefinition().getErrorSource());
            assertEquals("Generation source is external for all network errors", WErrorSource.EXTERNAL, e.getErrorDefinition().getGenerationSource());
        }
        t.join();
    }

    @Test
    public void testUnknownHostError() throws TException {
        try {
            unknownHostClient.getOwner(0);
            fail();
        } catch (WRuntimeException e) {
            assertEquals("Network timeout expected", WErrorType.UNAVAILABLE_RESULT, e.getErrorDefinition().getErrorType());
            assertEquals("Error returned for root client request", WErrorSource.INTERNAL, e.getErrorDefinition().getErrorSource());
            assertEquals("Generation source is external for all network errors", WErrorSource.EXTERNAL, e.getErrorDefinition().getGenerationSource());
        }
    }

}
