package com.rbkmoney.woody.thrift.impl.http.interceptor.ext;

import com.rbkmoney.woody.api.event.CompositeClientEventListener;
import com.rbkmoney.woody.api.event.CompositeServiceEventListener;
import com.rbkmoney.woody.api.flow.WFlow;
import com.rbkmoney.woody.api.flow.error.WErrorSource;
import com.rbkmoney.woody.api.flow.error.WErrorType;
import com.rbkmoney.woody.api.flow.error.WRuntimeException;
import com.rbkmoney.woody.api.generator.TimestampIdGenerator;
import com.rbkmoney.woody.api.trace.ContextUtils;
import com.rbkmoney.woody.api.trace.Metadata;
import com.rbkmoney.woody.api.trace.context.TraceContext;
import com.rbkmoney.woody.api.trace.context.metadata.MetadataConversionException;
import com.rbkmoney.woody.api.trace.context.metadata.MetadataConverter;
import com.rbkmoney.woody.api.trace.context.metadata.MetadataExtension;
import com.rbkmoney.woody.api.trace.context.metadata.MetadataExtensionKit;
import com.rbkmoney.woody.rpc.Owner;
import com.rbkmoney.woody.rpc.OwnerServiceSrv;
import com.rbkmoney.woody.thrift.impl.http.AbstractTest;
import com.rbkmoney.woody.thrift.impl.http.OwnerServiceStub;
import com.rbkmoney.woody.thrift.impl.http.event.ClientEventLogListener;
import com.rbkmoney.woody.thrift.impl.http.event.HttpClientEventLogListener;
import com.rbkmoney.woody.thrift.impl.http.event.HttpServiceEventLogListener;
import com.rbkmoney.woody.thrift.impl.http.event.ServiceEventLogListener;
import org.apache.thrift.TException;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.Servlet;
import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * Created by vpankrashkin on 24.01.17.
 */
public class TestCustomMetadataExtension extends AbstractTest {
    OwnerServiceSrv.Iface handler = new OwnerServiceStub() {
        @Override
        public Owner getOwner(int id) throws TException {
            switch (id) {
                case 0:
                    assertEquals("A", ContextUtils.getCustomMetadataValue(String.class, "1"));
                    assertNull("A", ContextUtils.getCustomMetadataValue(TraceContext.getCurrentTraceData().getClientSpan(), String.class, "1"));
                    ContextUtils.setCustomMetadataValue("2", "B");
                    client2.getOwner(1);
                    ContextUtils.setCustomMetadataValue("3", "EC");
                    break;
                case 1:
                    assertEquals("A", ContextUtils.getCustomMetadataValue(String.class, "1"));
                    assertEquals("B", ContextUtils.getCustomMetadataValue(String.class, "2"));
                    assertNull("A", ContextUtils.getCustomMetadataValue(TraceContext.getCurrentTraceData().getClientSpan(), String.class, "1"));
                    ContextUtils.setCustomMetadataValue("3", "C");
                    client3.getOwner(2);
                    break;
                case 2:
                    assertEquals("A", ContextUtils.getCustomMetadataValue(String.class, "1"));
                    assertEquals("B", ContextUtils.getCustomMetadataValue(String.class, "2"));
                    assertEquals("C", ContextUtils.getCustomMetadataValue(String.class, "3"));
                    assertNull("A", ContextUtils.getCustomMetadataValue(TraceContext.getCurrentTraceData().getClientSpan(), String.class, "1"));
                    break;
                case 10:
                    assertEquals((Object) 1, ContextUtils.getCustomMetadataValue(IntExtension.instance.getExtension()));
                    break;
                default:
                    super.getOwner(id);
            }
            return super.getOwner(id);
        }
    };

    OwnerServiceSrv.Iface rpcMetaClientToMetaSrv = createThriftRPCClient(OwnerServiceSrv.Iface.class, new TimestampIdGenerator(), new CompositeClientEventListener(new HttpClientEventLogListener(), new ClientEventLogListener()), Arrays.asList(IntExtension.instance), getUrlString("/rpc_cmeta"));
    OwnerServiceSrv.Iface rpcMetaClientToNoMetaSrv = createThriftRPCClient(OwnerServiceSrv.Iface.class, new TimestampIdGenerator(), new CompositeClientEventListener(new HttpClientEventLogListener(), new ClientEventLogListener()), Arrays.asList(IntExtension.instance), getUrlString("/rpc_no_cmeta"));
    OwnerServiceSrv.Iface rpcNoMetaClientToMetaSrv = createThriftRPCClient(OwnerServiceSrv.Iface.class, new TimestampIdGenerator(), new CompositeClientEventListener(new HttpClientEventLogListener(), new ClientEventLogListener()), getUrlString("/rpc_cmeta"));
    OwnerServiceSrv.Iface client1 = createThriftRPCClient(OwnerServiceSrv.Iface.class, new TimestampIdGenerator(), new CompositeClientEventListener(new HttpClientEventLogListener(), new ClientEventLogListener()), getUrlString("/rpc_no_cmeta"));
    OwnerServiceSrv.Iface client2 = createThriftRPCClient(OwnerServiceSrv.Iface.class, new TimestampIdGenerator(), new CompositeClientEventListener(new HttpClientEventLogListener(), new ClientEventLogListener()), getUrlString("/rpc_no_cmeta"));
    OwnerServiceSrv.Iface client3 = createThriftRPCClient(OwnerServiceSrv.Iface.class, new TimestampIdGenerator(), new CompositeClientEventListener(new HttpClientEventLogListener(), new ClientEventLogListener()), getUrlString("/rpc_no_cmeta"));

    Servlet cMetaServlet =  createThriftRPCService(OwnerServiceSrv.Iface.class, handler, new CompositeServiceEventListener(new HttpServiceEventLogListener(), new ServiceEventLogListener()), Arrays.asList(IntExtension.instance));
    Servlet ncMetaServlet =  createThriftRPCService(OwnerServiceSrv.Iface.class, handler, new CompositeServiceEventListener(new HttpServiceEventLogListener(), new ServiceEventLogListener()));


    @Before
    public void setUp() {
    }

    @Test
    public void testIsolation() {
        addServlet(ncMetaServlet, "/rpc_no_cmeta");

        new WFlow().createServiceFork(() -> {
            ContextUtils.setCustomMetadataValue("1", "A");
            try {
                assertEquals("A", ContextUtils.getCustomMetadataValue(Object.class, "1"));
                client1.getOwner(0);
                assertEquals("A", ContextUtils.getCustomMetadataValue(Object.class, "1"));
                assertNull(ContextUtils.getCustomMetadataValue(Object.class, "2"));
            } catch (TException e) {
                e.printStackTrace();
                fail();
            }
        }).run();
    }

    @Test
    public void testExtensionMeta() {
        addServlet(cMetaServlet, "/rpc_cmeta");

        new WFlow().createServiceFork(() -> {
            ContextUtils.setCustomMetadataValue(1, IntExtension.instance.getExtension());
            try {
                rpcMetaClientToMetaSrv.getOwner(10);
                assertEquals((Object) 1, ContextUtils.getCustomMetadataValue(IntExtension.instance.getExtension()));
            } catch (TException e) {
                e.printStackTrace();
                fail();
            }
        }).run();
    }

    @Test
    public void testCustomExtension() {
        addServlet(cMetaServlet, "/rpc_cmeta");

        new WFlow().createServiceFork(() -> {
            ContextUtils.setCustomMetadataValue(IntExtension.KEY, "1");
            try {
                rpcMetaClientToMetaSrv.getOwner(10);
                fail();
            } catch (TException e) {
                e.printStackTrace();
                fail();
            } catch (WRuntimeException e) {
                assertTrue(e.getErrorDefinition().getGenerationSource() == WErrorSource.INTERNAL);
                assertTrue(e.getErrorDefinition().getErrorSource() == WErrorSource.INTERNAL);
                assertTrue(e.getErrorDefinition().getErrorType() == WErrorType.UNEXPECTED_ERROR);
            }
        }).run();
    }

    @Test
    public void testCustomExtensionNoData() {
        addServlet(cMetaServlet, "/rpc_cmeta");

        new WFlow().createServiceFork(() -> {
            try {
                rpcMetaClientToMetaSrv.getOwner(10);
                fail();
            } catch (TException e) {
                e.printStackTrace();
                fail();
            } catch (WRuntimeException e) {
                assertTrue(e.getErrorDefinition().getGenerationSource() == WErrorSource.INTERNAL);
                assertTrue(e.getErrorDefinition().getErrorSource() == WErrorSource.INTERNAL);
                assertTrue(e.getErrorDefinition().getErrorType() == WErrorType.PROVIDER_ERROR);
            }
        }).run();
    }

    @Test
    public void testCustomExtensionSrvNoCMeta() {
        addServlet(ncMetaServlet, "/rpc_no_cmeta");

        new WFlow().createServiceFork(() -> {
            ContextUtils.setCustomMetadataValue(IntExtension.KEY, 1);
            try {
                rpcMetaClientToNoMetaSrv.getOwner(-1);
            } catch (TException e) {
                e.printStackTrace();
                fail();
            }
        }).run();
    }

    @Test
    public void testNoCustomExtensionSrvCMeta() {
        addServlet(cMetaServlet, "/rpc_cmeta");

        new WFlow().createServiceFork(() -> {
            try {
                rpcNoMetaClientToMetaSrv.getOwner(-1);
                fail();
            } catch (TException e) {
                e.printStackTrace();
                fail();
            } catch (WRuntimeException e) {
                assertEquals(WErrorSource.INTERNAL, e.getErrorDefinition().getErrorSource());
                assertEquals(WErrorSource.EXTERNAL, e.getErrorDefinition().getGenerationSource());
                assertEquals(WErrorType.PROVIDER_ERROR, e.getErrorDefinition().getErrorType());
            }
        }).run();
    }

    static class IntExtension implements MetadataExtensionKit<Integer> {
        private static final String KEY = "int-val";
        static final IntExtension instance = new IntExtension();

        private final boolean applyToObject;
        private final boolean applyToString;

        public IntExtension(boolean applyToObject, boolean applyToString) {
            this.applyToObject = applyToObject;
            this.applyToString = applyToString;
        }

        public IntExtension() {
            this(false, false);
        }

        @Override
        public MetadataExtension<Integer> getExtension() {
            return new MetadataExtension<Integer>() {
                @Override
                public Integer getValue(Metadata metadata) {
                    return metadata.getValue(KEY);
                }

                @Override
                public void setValue(Integer val, Metadata metadata) {
                    metadata.putValue(KEY, val);
                }
            };
        }

        @Override
        public MetadataConverter<Integer> getConverter() {
            return new MetadataConverter<Integer>() {
                @Override
                public Integer convertToObject(String key, String value) throws MetadataConversionException {
                    return Integer.parseInt(value);
                }

                @Override
                public String convertToString(String key, Integer value) throws MetadataConversionException {
                    return String.valueOf(value);
                }

                @Override
                public boolean apply(String key) {
                    return KEY.equalsIgnoreCase(key);
                }

                @Override
                public boolean applyToObject() {
                    return IntExtension.this.applyToObject;
                }

                @Override
                public boolean applyToString() {
                    return IntExtension.this.applyToString;
                }
            };
        }
    }

}
