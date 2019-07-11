package com.rbkmoney.woody.thrift.impl.http;

import com.rbkmoney.woody.api.flow.WFlow;
import com.rbkmoney.woody.api.flow.error.WErrorDefinition;
import com.rbkmoney.woody.api.flow.error.WErrorType;
import com.rbkmoney.woody.api.flow.error.WRuntimeException;
import com.rbkmoney.woody.api.trace.ContextUtils;
import com.rbkmoney.woody.api.trace.context.TraceContext;
import com.rbkmoney.woody.rpc.Owner;
import com.rbkmoney.woody.rpc.OwnerServiceSrv;
import com.rbkmoney.woody.rpc.test_error;
import com.rbkmoney.woody.thrift.impl.http.transport.THttpHeader;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClients;
import org.apache.thrift.TException;
import org.junit.Ignore;
import org.junit.Test;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Duration;
import java.time.Instant;
import java.util.IntSummaryStatistics;
import java.util.Optional;
import java.util.stream.IntStream;

import static com.rbkmoney.woody.thrift.impl.http.transport.THttpHeader.DEADLINE;
import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.*;

public class TestDeadlines extends AbstractTest {

    private final String servletContextPath = "/test_servlet";

    private final Servlet testServlet = createThriftRPCService(OwnerServiceSrv.Iface.class, new OwnerServiceSrv.Iface() {

        @Override
        public int getIntValue() throws TException {
            return 42;
        }

        @Override
        public Owner getOwner(int id) throws TException {
            try {
                Thread.sleep(id);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(ex);
            }
            return new Owner(id, "test");
        }

        @Override
        public Owner getErrOwner(int id) throws test_error, TException {
            throw new test_error();
        }

        @Override
        public void setOwner(Owner owner) throws TException {
            Instant deadline = ContextUtils.getDeadline(TraceContext.getCurrentTraceData().getServiceSpan());
            if (deadline != null && owner.getName() != null) {
                assertEquals(owner.getName(), deadline.toString());
            }
            OwnerServiceSrv.Iface client = createThriftRPCClient(OwnerServiceSrv.Iface.class, getUrlString(servletContextPath), owner.getId());
            switch (owner.getKey()) {
                case "USE_CURRENT":
                    client.setOwnerOneway(new Owner(owner.getId(), owner.getName()));
                    break;
                case "CHANGE_DEADLINE":
                    WFlow.create(() -> {
                        try {
                            Instant newDeadline = Optional.ofNullable(deadline)
                                    .map(time -> time.plusMillis(owner.getId()))
                                    .orElse(Instant.now().plusSeconds(Math.abs(owner.getId())));
                            ContextUtils.setDeadline(newDeadline);
                            client.setOwnerOneway(new Owner(owner.getId(), newDeadline.toString()));
                        } catch (TException ex) {
                            throw new RuntimeException(ex);
                        }
                    }).run();
                    break;
                default:
                    throw new RuntimeException();
            }
        }

        @Override
        public void setOwnerOneway(Owner owner) throws TException {
            Instant deadline = ContextUtils.getDeadline(TraceContext.getCurrentTraceData().getServiceSpan());
            if (deadline != null && owner.getName() != null) {
                assertEquals(owner.getName(), deadline.toString());
            }
        }

        @Override
        public Owner setErrOwner(Owner owner, int id) throws test_error, TException {
            throw new test_error();
        }
    });

    @Test
    public void testClientSendDeadlineHeader() throws Exception {
        Instant deadline = Instant.now().plusSeconds(30);
        addServlet(new HttpServlet() {
            @Override
            protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
                assertNotNull(request.getHeader(DEADLINE.getKey()));
                assertEquals(deadline.toString(), request.getHeader(DEADLINE.getKey()));
                writeResultMessage(request, response);
            }
        }, "/check_deadline_header");
        new WFlow().createServiceFork(
                () -> {
                    ContextUtils.setDeadline(deadline);
                    OwnerServiceSrv.Iface client = createThriftRPCClient(OwnerServiceSrv.Iface.class, getUrlString("/check_deadline_header"));
                    return client.getIntValue();
                }
        ).call();
    }

    @Test
    public void testWhenDeadlineNotSet() throws Exception {
        int timeout = 5000;
        addServlet(new HttpServlet() {
            @Override
            protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
                assertNotNull(request.getHeader(DEADLINE.getKey()));
                writeResultMessage(request, response);
            }
        }, "/check_when_deadline_not_set");
        OwnerServiceSrv.Iface client = createThriftRPCClient(OwnerServiceSrv.Iface.class, getUrlString("/check_when_deadline_not_set"), timeout);
        client.getIntValue();
    }

    @Test
    public void testServerReceiveDeadline() throws Exception {
        addServlet(testServlet, servletContextPath);
        Instant deadline = Instant.now().plusSeconds(20);
        HttpClient httpClient = HttpClients.custom()
                .addInterceptorFirst((HttpResponseInterceptor) (response, context) -> {
                    assertTrue(response.containsHeader(DEADLINE.getKey()));
                    assertEquals(deadline.toString(), response.getLastHeader(DEADLINE.getKey()).getValue());
                }).build();
        OwnerServiceSrv.Iface client = createThriftRPCClient(OwnerServiceSrv.Iface.class, getUrlString(servletContextPath), httpClient);
        new WFlow().createServiceFork(
                () -> {
                    ContextUtils.setDeadline(deadline);
                    return client.getIntValue();
                }
        ).call();
    }

    @Test
    public void testWhenDeadlineHeaderIsIncorrect() throws Exception {
        addServlet(testServlet, servletContextPath);
        HttpClient httpClient = HttpClients.custom()
                .addInterceptorFirst((HttpRequestInterceptor) (request, context) -> {
                    assertTrue(request.containsHeader(THttpHeader.DEADLINE.getKey()));
                    request.setHeader(THttpHeader.DEADLINE.getKey(), "%%?%%???");
                }).build();
        OwnerServiceSrv.Iface client = createThriftRPCClient(OwnerServiceSrv.Iface.class, getUrlString(servletContextPath), httpClient);
        try {
            client.getIntValue();
            fail();
        } catch (WRuntimeException ex) {
            WErrorDefinition errorDefinition = ex.getErrorDefinition();
            assertEquals(WErrorType.UNEXPECTED_ERROR, errorDefinition.getErrorType());
            assertEquals("bad header: woody.deadline", errorDefinition.getErrorReason());
        }
    }

    @Test
    public void testWhenDeadlineIsReachedOnClient() throws Exception {
        Instant deadline = Instant.now().minusSeconds(5);
        OwnerServiceSrv.Iface client = createThriftRPCClient(OwnerServiceSrv.Iface.class, getUrlString(servletContextPath));
        try {
            new WFlow().createServiceFork(
                    () -> {
                        ContextUtils.setDeadline(deadline);
                        return client.getIntValue();
                    }
            ).call();
            fail();
        } catch (WRuntimeException ex) {
            WErrorDefinition errorDefinition = ex.getErrorDefinition();
            assertEquals(WErrorType.UNAVAILABLE_RESULT, errorDefinition.getErrorType());
            assertEquals("deadline reached", errorDefinition.getErrorReason());
        }
    }

    @Test
    public void testWhenDeadlineIsReachedOnServer() throws Exception {
        addServlet(testServlet, servletContextPath);

        HttpClient httpClient = HttpClients.custom()
                .addInterceptorFirst(
                        (HttpRequestInterceptor) (request, context) -> request.setHeader(DEADLINE.getKey(), Instant.now().minusSeconds(5).toString())
                ).build();
        OwnerServiceSrv.Iface client = createThriftRPCClient(OwnerServiceSrv.Iface.class, getUrlString(servletContextPath), httpClient);
        try {
            client.getErrOwner(1);
            fail();
        } catch (WRuntimeException ex) {
            WErrorDefinition errorDefinition = ex.getErrorDefinition();
            assertEquals(WErrorType.UNAVAILABLE_RESULT, errorDefinition.getErrorType());
            assertEquals("deadline reached", errorDefinition.getErrorReason());
        }
    }

    @Test
    @Ignore
    public void testConnectTimeout() throws Exception {
        ServerSocket serverSocket = new ServerSocket(0, 1);
        Socket socket = new Socket();
        socket.setKeepAlive(true);
        socket.connect(serverSocket.getLocalSocketAddress());
        OwnerServiceSrv.Iface client = createThriftRPCClient(OwnerServiceSrv.Iface.class, "http://localhost:" + serverSocket.getLocalPort(), 200);
        try {
            client.getIntValue();
        } catch (WRuntimeException ex) {
            WErrorDefinition errorDefinition = ex.getErrorDefinition();
            assertEquals(WErrorType.UNDEFINED_RESULT, errorDefinition.getErrorType());
            assertEquals("java.net.SocketTimeoutException: connect timed out", errorDefinition.getErrorReason());
            assertEquals("connect timed out", errorDefinition.getErrorMessage());
        }
        if (!serverSocket.isClosed()) {
            serverSocket.close();
        }
    }

    @Test
    public void testWhenTimeoutNotSet() throws TException {
        int timeout = -1;
        addServlet(testServlet, servletContextPath);
        HttpClient httpClient = HttpClients.custom()
                .addInterceptorFirst((HttpRequestInterceptor) (request, context) -> {
                    assertFalse(request.containsHeader(DEADLINE.getKey()));
                })
                .addInterceptorFirst((HttpResponseInterceptor) (response, context) -> {
                    assertFalse(response.containsHeader(DEADLINE.getKey()));
                }).build();
        OwnerServiceSrv.Iface client = createThriftRPCClient(OwnerServiceSrv.Iface.class, getUrlString(servletContextPath), timeout, httpClient);
        Owner owner = new Owner(timeout, null);
        owner.setKey("USE_CURRENT");
        client.setOwner(owner);
    }

    @Test
    public void testDeadlinesTimings() throws TException {
        addServlet(testServlet, servletContextPath);
        int timeout = 1000;

        OwnerServiceSrv.Iface client = createThriftRPCClient(OwnerServiceSrv.Iface.class, getUrlString(servletContextPath), timeout);
        client.getOwner(1);
        IntSummaryStatistics summaryStatistics = IntStream.range(1, 10)
                .map(current -> {
                    try {
                        return new WFlow().createServiceFork(
                                () -> {
                                    try {
                                        ContextUtils.setDeadline(Instant.now().plusMillis(timeout));
                                        client.getOwner(timeout + 100);
                                        fail();
                                        return -1;
                                    } catch (WRuntimeException ex) {
                                        WErrorDefinition errorDefinition = ex.getErrorDefinition();
                                        assertEquals(WErrorType.UNDEFINED_RESULT, errorDefinition.getErrorType());
                                        assertEquals("java.net.SocketTimeoutException: Read timed out", errorDefinition.getErrorReason());
                                        assertEquals("Read timed out", errorDefinition.getErrorMessage());
                                        return (int) Duration.between(
                                                ContextUtils.getDeadline(TraceContext.getCurrentTraceData().getClientSpan()),
                                                Instant.now()
                                        ).abs().toMillis();
                                    }
                                }
                        ).call();
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                }).summaryStatistics();

        assertTrue(summaryStatistics.getMax() < timeout / 20);
    }

    @Test
    public void testWhenDeadlineSendFromService() {
        addServlet(testServlet, servletContextPath);
        int timeout = 30000;
        Instant deadline = Instant.now().plusMillis(timeout);
        OwnerServiceSrv.Iface client = createThriftRPCClient(OwnerServiceSrv.Iface.class, getUrlString(servletContextPath), timeout);
        new WFlow().createServiceFork(
                () -> {
                    try {
                        ContextUtils.setDeadline(deadline);
                        Owner owner = new Owner(timeout, deadline.toString());
                        owner.setKey("USE_CURRENT");
                        client.setOwner(owner);
                    } catch (TException ex) {
                        throw new RuntimeException(ex);
                    }
                }
        ).run();
        new WFlow().createServiceFork(
                () -> {
                    try {
                        ContextUtils.setDeadline(deadline);
                        Owner owner = new Owner(timeout, deadline.toString());
                        owner.setKey("CHANGE_DEADLINE");
                        client.setOwner(owner);
                    } catch (TException ex) {
                        throw new RuntimeException(ex);
                    }
                }
        ).run();
    }

}