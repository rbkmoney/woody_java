package com.rbkmoney.woody.thrift.impl.http;

import com.rbkmoney.woody.api.event.ClientEventListener;
import com.rbkmoney.woody.api.flow.error.WErrorDefinition;
import com.rbkmoney.woody.api.flow.error.WErrorSource;
import com.rbkmoney.woody.api.flow.error.WErrorType;
import com.rbkmoney.woody.api.generator.TimestampIdGenerator;
import com.rbkmoney.woody.rpc.OwnerServiceSrv;
import com.rbkmoney.woody.thrift.impl.http.event.THClientEvent;
import org.apache.thrift.TException;
import org.junit.Test;

import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

public class TestNginxError extends AbstractTest {
    @Test
    public void testNginx500Error() throws TException {
        addServlet(new HttpServlet() {
            @Override
            protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
                throw new RuntimeException("Unexpected nginx error");
            }
        }, "/");
        AtomicBoolean hasErr = new AtomicBoolean();
        OwnerServiceSrv.Iface client = createThriftRPCClient(OwnerServiceSrv.Iface.class, new TimestampIdGenerator(), (ClientEventListener<THClientEvent>) (THClientEvent thClientEvent) -> {
            switch (thClientEvent.getEventType()) {
                case ERROR:
                    assertFalse(thClientEvent.isSuccessfulCall());
                    assertEquals(new Integer(500), thClientEvent.getThriftResponseStatus());
                    WErrorDefinition errorDefinition = thClientEvent.getErrorDefinition();
                    assertEquals(errorDefinition.getErrorSource(), WErrorSource.INTERNAL);
                    assertEquals(errorDefinition.getGenerationSource(), WErrorSource.EXTERNAL);
                    assertEquals(errorDefinition.getErrorType(), WErrorType.UNEXPECTED_ERROR);
                    assertNull(errorDefinition.getErrorReason());
                    assertNull(errorDefinition.getErrorName());
                    hasErr.set(true);
                    break;
            }
        });
        try {
            client.getOwner(0);
            fail();
        } catch (RuntimeException e) {
            assertTrue(hasErr.get());
            e.printStackTrace();
        }
    }
    @Test
    public void testNginxOk() throws TException {
        addServlet(new HttpServlet() {
            @Override
            protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
                resp.getWriter().write("OK");
            }
        }, "/");
        AtomicBoolean hasErr = new AtomicBoolean();
        OwnerServiceSrv.Iface client = createThriftRPCClient(OwnerServiceSrv.Iface.class, new TimestampIdGenerator(), (ClientEventListener<THClientEvent>) (THClientEvent thClientEvent) -> {
            switch (thClientEvent.getEventType()) {
                case ERROR:
                    assertFalse(thClientEvent.isSuccessfulCall());
                    assertEquals(new Integer(HttpServletResponse.SC_OK), thClientEvent.getThriftResponseStatus());
                    WErrorDefinition errorDefinition = thClientEvent.getErrorDefinition();
                    assertEquals(WErrorSource.INTERNAL, errorDefinition.getErrorSource());
                    assertEquals(WErrorSource.INTERNAL, errorDefinition.getGenerationSource());
                    assertEquals(WErrorType.PROVIDER_ERROR, errorDefinition.getErrorType());
                    hasErr.set(true);
                    break;
            }
        });
        try {
            client.getOwner(0);
            fail();
        } catch (RuntimeException e) {
            assertTrue(hasErr.get());
            e.printStackTrace();
        }
    }
    @Test
    public void testNginx502Error() throws TException {
        addServlet(new HttpServlet() {
            @Override
            protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
                resp.sendError(HttpServletResponse.SC_BAD_GATEWAY);
            }
        }, "/");
        AtomicBoolean hasErr = new AtomicBoolean();
        OwnerServiceSrv.Iface client = createThriftRPCClient(OwnerServiceSrv.Iface.class, new TimestampIdGenerator(), (ClientEventListener<THClientEvent>) (THClientEvent thClientEvent) -> {
            switch (thClientEvent.getEventType()) {
                case ERROR:
                    assertFalse(thClientEvent.isSuccessfulCall());
                    assertEquals(new Integer(HttpServletResponse.SC_BAD_GATEWAY), thClientEvent.getThriftResponseStatus());
                    WErrorDefinition errorDefinition = thClientEvent.getErrorDefinition();
                    assertEquals(WErrorSource.EXTERNAL, errorDefinition.getErrorSource());
                    assertEquals(WErrorSource.EXTERNAL, errorDefinition.getGenerationSource());
                    assertEquals(WErrorType.UNEXPECTED_ERROR, errorDefinition.getErrorType());
                    assertNull(errorDefinition.getErrorReason());
                    assertNull(errorDefinition.getErrorName());
                    hasErr.set(true);
                    break;
            }
        });
        try {
            client.getOwner(0);
            fail();
        } catch (RuntimeException e) {
            assertTrue(hasErr.get());
            e.printStackTrace();
        }
    }
    @Test
    public void testNginx4xxError() throws TException {
        addServlet(new HttpServlet() {
            @Override
            protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
                resp.sendError(HttpServletResponse.SC_CONFLICT);
            }
        }, "/");
        AtomicBoolean hasErr = new AtomicBoolean();
        OwnerServiceSrv.Iface client = createThriftRPCClient(OwnerServiceSrv.Iface.class, new TimestampIdGenerator(), (ClientEventListener<THClientEvent>) (THClientEvent thClientEvent) -> {
            switch (thClientEvent.getEventType()) {
                case ERROR:
                    assertFalse(thClientEvent.isSuccessfulCall());
                    assertEquals(new Integer(HttpServletResponse.SC_CONFLICT), thClientEvent.getThriftResponseStatus());
                    WErrorDefinition errorDefinition = thClientEvent.getErrorDefinition();
                    assertEquals(WErrorSource.INTERNAL, errorDefinition.getErrorSource());
                    assertEquals(WErrorSource.EXTERNAL, errorDefinition.getGenerationSource());
                    assertEquals(WErrorType.UNEXPECTED_ERROR, errorDefinition.getErrorType());
                    assertNull(errorDefinition.getErrorReason());
                    assertNull(errorDefinition.getErrorName());
                    hasErr.set(true);
                    break;
            }
        });
        try {
            client.getOwner(0);
            fail();
        } catch (RuntimeException e) {
            assertTrue(hasErr.get());
            e.printStackTrace();
        }
    }
}
