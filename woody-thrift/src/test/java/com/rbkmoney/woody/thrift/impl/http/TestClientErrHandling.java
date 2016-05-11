package com.rbkmoney.woody.thrift.impl.http;

import com.rbkmoney.woody.api.event.CallType;
import com.rbkmoney.woody.api.event.ClientEvent;
import com.rbkmoney.woody.api.event.ErrorType;
import com.rbkmoney.woody.api.generator.IdGenerator;
import com.rbkmoney.woody.rpc.Owner;
import com.rbkmoney.woody.rpc.OwnerService;
import com.rbkmoney.woody.rpc.err_one;
import com.rbkmoney.woody.thrift.impl.http.event.THClientEvent;
import org.apache.thrift.TException;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by vpankrashkin on 06.05.16.
 */
public class TestClientErrHandling extends AbstractClientTest {
    {

        tProcessor = new OwnerService.Processor<>(new

                                                          OwnerServiceStub() {
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
                                                              public Owner getErrOwner(int id) throws err_one {
                                                                  throw new err_one(id);
                                                              }
                                                          }

        );
    }

    @Test
    public void testUnexpectedError() {

        OwnerService.Iface client = (OwnerService.Iface) createThriftRPCClient(OwnerService.Iface.class, new IdGeneratorStub(), (ClientEvent clientEvent) -> {
            THClientEvent thClientEvent = (THClientEvent) clientEvent;
            switch (thClientEvent.getEventType()) {
                case CALL_SERVICE:
                    assertArrayEquals(new Object[]{0}, thClientEvent.getCallArguments());
                    assertEquals("getOwner", thClientEvent.getCallName());
                    assertEquals(CallType.CALL, thClientEvent.getCallType());
                    assertEquals(IdGenerator.NO_PARENT_ID, thClientEvent.getParentId());
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
                    assertEquals(ErrorType.PROVIDER_ERROR, thClientEvent.getErrorType());
                    assertEquals(TErrorType.PROTOCOL, thClientEvent.getThriftErrorType());
                default:
            }


        });
        try {
            client.getOwner(0);
        } catch (TException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testExpectedError() {

        OwnerService.Iface client = (OwnerService.Iface) createThriftRPCClient(OwnerService.Iface.class, new IdGeneratorStub(), (ClientEvent clientEvent) -> {
            THClientEvent thClientEvent = (THClientEvent) clientEvent;
            switch (thClientEvent.getEventType()) {
                case CALL_SERVICE:
                    assertArrayEquals(new Object[]{0}, thClientEvent.getCallArguments());
                    assertEquals("getErrOwner", thClientEvent.getCallName());
                    assertEquals(CallType.CALL, thClientEvent.getCallType());
                    assertEquals(IdGenerator.NO_PARENT_ID, thClientEvent.getParentId());
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
                    fail("Should not be invoked on error");
                    break;
                case ERROR:
                    assertFalse(thClientEvent.isSuccessfullCall());
                    assertEquals(ErrorType.APPLICATION_KNOWN_ERROR, thClientEvent.getErrorType());
                    assertEquals("err_one", thClientEvent.getErrorName());
                    assertNull(thClientEvent.getThriftErrorType());
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

        OwnerService.Iface client = (OwnerService.Iface) createThriftRPCClient(OwnerService.Iface.class, new IdGeneratorStub(), (ClientEvent clientEvent) -> {
            THClientEvent thClientEvent = (THClientEvent) clientEvent;
            switch (thClientEvent.getEventType()) {
                case CALL_SERVICE:
                    assertArrayEquals(new Object[]{0}, thClientEvent.getCallArguments());
                    assertEquals("getOwner", thClientEvent.getCallName());
                    assertEquals(CallType.CALL, thClientEvent.getCallType());
                    assertEquals(IdGenerator.NO_PARENT_ID, thClientEvent.getParentId());
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
                    fail("Should not be invoked on success");
                default:
                    fail();
            }


        });
        try {
            client.getOwner(1);
        } catch (TException e) {
            e.printStackTrace();
        }

    }

}
