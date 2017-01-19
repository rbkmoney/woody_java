package com.rbkmoney.woody.thrift.impl.http;

import com.rbkmoney.woody.api.event.CallType;
import com.rbkmoney.woody.api.event.ClientEventListener;
import com.rbkmoney.woody.api.flow.error.WErrorSource;
import com.rbkmoney.woody.api.flow.error.WErrorType;
import com.rbkmoney.woody.api.flow.error.WRuntimeException;
import com.rbkmoney.woody.api.trace.context.TraceContext;
import com.rbkmoney.woody.rpc.Owner;
import com.rbkmoney.woody.rpc.OwnerServiceSrv;
import com.rbkmoney.woody.rpc.test_error;
import com.rbkmoney.woody.thrift.impl.http.event.THClientEvent;
import com.rbkmoney.woody.api.generator.TimestampIdGenerator;
import org.apache.thrift.TException;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

/**
 * Created by vpankrashkin on 06.05.16.
 */
public class TestClientEventHandling extends AbstractTest {

        OwnerServiceSrv.Iface handler = new OwnerServiceStub() {
            @Override
            public Owner getOwner(int id) throws TException {
                switch (id) {
                    case 0:
                        throw new RuntimeException("err");
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



    @Test
    public void testExpectedError() {
        addServlet(createMutableTServlet(OwnerServiceSrv.Iface.class, handler), "/");
        AtomicInteger order = new AtomicInteger();

        OwnerServiceSrv.Iface client = createThriftRPCClient(OwnerServiceSrv.Iface.class, new TimestampIdGenerator(), (ClientEventListener<THClientEvent>) (THClientEvent thClientEvent) -> {
            switch (thClientEvent.getEventType()) {
                case CALL_SERVICE:
                    assertEquals(0, order.getAndIncrement());
                    assertArrayEquals(new Object[]{0}, thClientEvent.getCallArguments());
                    assertEquals("getErrOwner", thClientEvent.getCallName());
                    assertEquals(CallType.CALL, thClientEvent.getCallType());
                    assertEquals(TraceContext.NO_PARENT_ID, thClientEvent.getParentId());
                    assertNotNull(thClientEvent.getTraceId());
                    assertEquals(thClientEvent.getTraceId(), thClientEvent.getSpanId());
                    assertNull(thClientEvent.getEndpoint());
                    assertNotEquals(thClientEvent.getTimeStamp(), 0);
                    break;
                case CLIENT_SEND:
                    assertEquals(1, order.getAndIncrement());
                    assertEquals(getUrlString(), thClientEvent.getEndpoint().getStringValue());
                    break;
                case CLIENT_RECEIVE:
                    assertEquals(2, order.getAndIncrement());
                    assertEquals(new Integer(200), thClientEvent.getThriftResponseStatus());
                    assertEquals("OK", thClientEvent.getThriftResponseMessage());
                    break;
                case SERVICE_RESULT:
                    fail("Should not be invoked on error");
                    break;
                case ERROR:
                    assertEquals(3, order.getAndIncrement());
                    assertFalse(thClientEvent.isSuccessfullCall());
                    assertEquals(WErrorType.BUSINESS_ERROR, thClientEvent.getErrorDefinition().getErrorType());
                    assertEquals("test_error", thClientEvent.getErrorDefinition().getErrorName());
                    assertNull(thClientEvent.getThriftErrorType());
                    break;
                default:
                    fail();
            }


        });
        try {
            client.getErrOwner(0);
        } catch (TException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testGetOwnerOK() {
        addServlet(createMutableTServlet(OwnerServiceSrv.Iface.class, handler), "/");

        OwnerServiceSrv.Iface client = createThriftRPCClient(OwnerServiceSrv.Iface.class, new TimestampIdGenerator(), (ClientEventListener<THClientEvent>) (THClientEvent thClientEvent) -> {
            switch (thClientEvent.getEventType()) {
                case CALL_SERVICE:
                    assertArrayEquals(new Object[]{1}, thClientEvent.getCallArguments());
                    assertEquals("getOwner", thClientEvent.getCallName());
                    assertEquals(CallType.CALL, thClientEvent.getCallType());
                    assertEquals(TraceContext.NO_PARENT_ID, thClientEvent.getParentId());
                    assertNotNull(thClientEvent.getTraceId());
                    assertEquals(thClientEvent.getTraceId(), thClientEvent.getSpanId());
                    assertNull(thClientEvent.getEndpoint());
                    assertNotEquals(thClientEvent.getTimeStamp(), 0);
                    break;
                case CLIENT_SEND:
                    assertEquals(getUrlString(), thClientEvent.getEndpoint().getStringValue());
                    break;
                case CLIENT_RECEIVE:
                    assertEquals(new Integer(200), thClientEvent.getThriftResponseStatus());
                    assertEquals("OK", thClientEvent.getThriftResponseMessage());
                    break;
                case SERVICE_RESULT:
                    assertEquals(new Owner(1, "name1"), thClientEvent.getCallResult());
                    break;
                case ERROR:
                default:
                    fail("Should not be invoked on success");
            }


        });
        try {
            client.getOwner(1);
        } catch (TException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testUnexpectedError() {
        addServlet(createMutableTServlet(OwnerServiceSrv.Iface.class, handler), "/");

        OwnerServiceSrv.Iface client = createThriftRPCClient(OwnerServiceSrv.Iface.class, new TimestampIdGenerator(), (ClientEventListener<THClientEvent>) (THClientEvent thClientEvent) -> {
            switch (thClientEvent.getEventType()) {
                case CALL_SERVICE:
                    assertArrayEquals(new Object[]{0}, thClientEvent.getCallArguments());
                    assertEquals("getOwner", thClientEvent.getCallName());
                    assertEquals(CallType.CALL, thClientEvent.getCallType());
                    assertEquals(TraceContext.NO_PARENT_ID, thClientEvent.getParentId());
                    assertNotNull(thClientEvent.getTraceId());
                    assertEquals(thClientEvent.getTraceId(), thClientEvent.getSpanId());
                    assertNull(thClientEvent.getEndpoint());
                    assertNotEquals(thClientEvent.getTimeStamp(), 0);
                    break;
                case CLIENT_SEND:
                    assertEquals(getUrlString(), thClientEvent.getEndpoint().getStringValue());
                    break;
                case CLIENT_RECEIVE:
                    assertEquals(new Integer(500), thClientEvent.getThriftResponseStatus());
                    assertEquals("Server Error", thClientEvent.getThriftResponseMessage());
                    break;
                case SERVICE_RESULT:
                    fail("Should not be invoked on error");
                    break;
                case ERROR:
                    assertFalse(thClientEvent.isSuccessfullCall());
                    assertEquals(WErrorType.UNEXPECTED_ERROR, thClientEvent.getErrorDefinition().getErrorType());
                    assertEquals("Error was generated outside of the client", WErrorSource.EXTERNAL, thClientEvent.getErrorDefinition().getGenerationSource());
                    assertEquals("This is internal service error", WErrorSource.INTERNAL, thClientEvent.getErrorDefinition().getErrorSource());
                    assertEquals("RuntimeException:err", thClientEvent.getErrorDefinition().getErrorReason());
                    break;
                default:
                    fail();
            }


        });
        try {
            client.getOwner(0);
        } catch (TException | WRuntimeException e) {
            e.printStackTrace();
        }
    }

}
