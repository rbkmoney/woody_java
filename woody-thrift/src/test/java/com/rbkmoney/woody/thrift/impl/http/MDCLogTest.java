package com.rbkmoney.woody.thrift.impl.http;

import com.rbkmoney.woody.api.MDCUtils;
import com.rbkmoney.woody.api.event.*;
import com.rbkmoney.woody.api.flow.error.*;
import com.rbkmoney.woody.api.trace.ContextUtils;
import com.rbkmoney.woody.rpc.Owner;
import com.rbkmoney.woody.rpc.OwnerServiceSrv;
import com.rbkmoney.woody.rpc.test_error;
import com.rbkmoney.woody.thrift.impl.http.event.*;
import com.rbkmoney.woody.api.generator.TimestampIdGenerator;
import org.apache.thrift.TException;
import org.easymock.internal.matchers.Null;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.servlet.Servlet;

import static java.lang.System.out;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * Created by tolkonepiu on 09.06.16.
 */
public class MDCLogTest extends AbstractTest {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    ClientEventListener clientEventListener = (ClientEventListener<THClientEvent>) event -> {
        assertNotNull(MDC.get(MDCUtils.SPAN_ID));
        assertNotNull(MDC.get(MDCUtils.TRACE_ID));
        assertNotNull(MDC.get(MDCUtils.PARENT_ID));

        assertEquals(MDC.get(MDCUtils.SPAN_ID), event.getSpanId());
        assertEquals(MDC.get(MDCUtils.TRACE_ID), event.getTraceId());
        assertEquals(MDC.get(MDCUtils.PARENT_ID), event.getParentId());
    };

    ServiceEventListener serviceEventListener = (ServiceEventListener<THServiceEvent>) event -> {
        assertNotNull(MDC.get(MDCUtils.SPAN_ID));
        assertNotNull(MDC.get(MDCUtils.TRACE_ID));
        assertNotNull(MDC.get(MDCUtils.PARENT_ID));

        assertEquals(MDC.get(MDCUtils.SPAN_ID), event.getSpanId());
        assertEquals(MDC.get(MDCUtils.TRACE_ID), event.getTraceId());
        assertEquals(MDC.get(MDCUtils.PARENT_ID), event.getParentId());
    };

    OwnerServiceSrv.Iface client1 = createThriftRPCClient(OwnerServiceSrv.Iface.class, new TimestampIdGenerator(), new CompositeClientEventListener(new HttpClientEventLogListener(), new ClientEventLogListener(), clientEventListener), getUrlString("/rpc"));
    OwnerServiceSrv.Iface client2 = createThriftRPCClient(OwnerServiceSrv.Iface.class, new TimestampIdGenerator(), new CompositeClientEventListener(new HttpClientEventLogListener(), new ClientEventLogListener(), clientEventListener), getUrlString("/rpc"));
    OwnerServiceSrv.Iface handler = new OwnerServiceStub() {
        @Override
        public Owner getErrOwner(int id) throws TException, test_error {
            switch (id) {
                case 0:
                    Owner owner = client2.getOwner(0);
                    client2.setOwnerOneway(owner);
                    return client2.getOwner(10);
                case 200:
                    throw new test_error(200);
                case 201:
                    return client2.getErrOwner(202);
                case 202:
                    throw new WUnavailableResultException("Fake WUnavailableResultException");
                case 203:
                    return client2.getErrOwner(204);
                case 204:
                    throw new WUndefinedResultException("Fake WUndefinedResultException");
                case 500:
                    throw new RuntimeException("Test");
                default:
                    return super.getErrOwner(id);
            }
        }
    };

    Servlet servlet = createThriftRPCService(OwnerServiceSrv.Iface.class, handler, new CompositeServiceEventListener(new HttpServiceEventLogListener(), new ServiceEventLogListener(), serviceEventListener));

    @Before
    public void before() {
        addServlet(servlet, "/rpc");
    }

    @Test
    public void testMDCContext() throws TException {
        out.println("Root call>");
        Assert.assertEquals(new Owner(10, "10"), client1.getErrOwner(0));
        out.println("<");

        out.println("Root call>");
        try {
            client1.getErrOwner(200);
            Assert.fail();
        } catch (test_error e) {
        }

        out.println("Root call>");
        try {
            client1.getErrOwner(201);
            Assert.fail();
        } catch (WUnavailableResultException e) {
            assertEquals(e.getErrorDefinition().getGenerationSource(), WErrorSource.EXTERNAL);
            assertEquals(e.getErrorDefinition().getErrorSource(), WErrorSource.INTERNAL);
            assertEquals(e.getErrorDefinition().getErrorType(), WErrorType.UNAVAILABLE_RESULT);
        }

        out.println("Root call>");
        try {
            client1.getErrOwner(203);
            Assert.fail();
        } catch (WUndefinedResultException e) {
            assertEquals(e.getErrorDefinition().getGenerationSource(), WErrorSource.EXTERNAL);
            assertEquals(e.getErrorDefinition().getErrorSource(), WErrorSource.INTERNAL);
            assertEquals(e.getErrorDefinition().getErrorType(), WErrorType.UNDEFINED_RESULT);
        }

        out.println("<");
        out.println("Root call>");
        try {
            client1.getErrOwner(500);
            fail();
        } catch (WRuntimeException e) {
            assertEquals(e.getErrorDefinition().getGenerationSource(), WErrorSource.EXTERNAL);
            assertEquals(e.getErrorDefinition().getErrorSource(), WErrorSource.INTERNAL);
            assertEquals(e.getErrorDefinition().getErrorType(), WErrorType.UNEXPECTED_ERROR);
        }
        out.println("<");
    }
}
