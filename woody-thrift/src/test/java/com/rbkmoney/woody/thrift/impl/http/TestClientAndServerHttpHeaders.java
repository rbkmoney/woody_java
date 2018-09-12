package com.rbkmoney.woody.thrift.impl.http;

import com.rbkmoney.woody.api.flow.WFlow;
import com.rbkmoney.woody.api.trace.ContextUtils;
import com.rbkmoney.woody.api.trace.Metadata;
import com.rbkmoney.woody.api.trace.context.metadata.MetadataConversionException;
import com.rbkmoney.woody.api.trace.context.metadata.MetadataConverter;
import com.rbkmoney.woody.api.trace.context.metadata.MetadataExtension;
import com.rbkmoney.woody.api.trace.context.metadata.MetadataExtensionKit;
import com.rbkmoney.woody.rpc.OwnerServiceSrv;
import com.rbkmoney.woody.thrift.impl.http.transport.THttpHeader;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TIOStreamTransport;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.junit.Test;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Map;

import static org.junit.Assert.*;

public class TestClientAndServerHttpHeaders extends AbstractTest {

    @Test
    public void testCheckClientTraceHeaders() throws TException {
        Servlet servlet = new DefaultServlet() {

            @Override
            public void service(ServletRequest servletRequest, ServletResponse servletResponse) throws ServletException, IOException {
                for (THttpHeader tHttpHeader : Arrays.asList(THttpHeader.SPAN_ID, THttpHeader.TRACE_ID, THttpHeader.PARENT_ID)) {
                    assertNotNull(((Request) servletRequest).getHeader(tHttpHeader.getKey()));
                    assertEquals(((Request) servletRequest).getHeader(tHttpHeader.getKey()), ((Request) servletRequest).getHeader(tHttpHeader.getOldKey()));
                }
                writeResultMessage(servletRequest, servletResponse);
            }
        };

        addServlet(servlet, "/check_trace_headers");
        OwnerServiceSrv.Iface client = createThriftRPCClient(OwnerServiceSrv.Iface.class, getUrlString("/check_trace_headers"));
        client.getIntValue();
        servlet.destroy();
    }

    @Test
    public void testCheckClientMetaHeaders() {
        Map.Entry<String, String> metadataWithoutExtensionKit = new AbstractMap.SimpleEntry<>("test", "test-value");
        Map.Entry<String, String> metadataWithExtensionKit = new AbstractMap.SimpleEntry<>("extension-test", "extension-test-value");
        MetadataExtensionKit metadataExtensionTestKit = new MetadataExtensionKit<String>() {

            @Override
            public MetadataExtension<String> getExtension() {
                return new MetadataExtension<String>() {
                    @Override
                    public String getValue(Metadata metadata) {
                        return metadata.getValue(metadataWithExtensionKit.getKey());
                    }

                    @Override
                    public void setValue(String val, Metadata metadata) {
                        metadata.putValue(metadataWithExtensionKit.getKey(), val);
                    }
                };
            }

            @Override
            public MetadataConverter<String> getConverter() {
                return new MetadataConverter<String>() {
                    @Override
                    public String convertToObject(String key, String value) throws MetadataConversionException {
                        return value;
                    }

                    @Override
                    public String convertToString(String key, String value) throws MetadataConversionException {
                        return value;
                    }

                    @Override
                    public boolean apply(String key) {
                        return metadataWithExtensionKit.getKey().equalsIgnoreCase(key);
                    }
                };
            }
        };

        Servlet servlet = new DefaultServlet() {

            @Override
            public void service(ServletRequest servletRequest, ServletResponse servletResponse) throws ServletException, IOException {
                Request request = (Request) servletRequest;
                assertEquals(request.getHeader(THttpHeader.META.getKey() + metadataWithExtensionKit.getKey()), metadataWithExtensionKit.getValue());
                assertEquals(request.getHeader(THttpHeader.META.getOldKey() + metadataWithExtensionKit.getKey()), metadataWithExtensionKit.getValue());

                assertEquals(request.getHeader(THttpHeader.META.getKey() + metadataWithoutExtensionKit.getKey()), metadataWithoutExtensionKit.getValue());
                assertEquals(request.getHeader(THttpHeader.META.getOldKey() + metadataWithoutExtensionKit.getKey()), metadataWithoutExtensionKit.getValue());

                writeResultMessage(servletRequest, servletResponse);
            }
        };

        addServlet(servlet, "/check_meta_headers");
        OwnerServiceSrv.Iface client = createThriftRPCClient(OwnerServiceSrv.Iface.class, Arrays.asList(metadataExtensionTestKit), getUrlString("/check_meta_headers"));

        new WFlow().createServiceFork(() -> {
            ContextUtils.setCustomMetadataValue(metadataWithoutExtensionKit.getKey(), metadataWithoutExtensionKit.getValue());
            ContextUtils.setCustomMetadataValue(metadataWithExtensionKit.getKey(), metadataWithExtensionKit.getValue());
            try {
                client.getIntValue();
            } catch (TException e) {
                e.printStackTrace();
                fail();
            }
        }).run();
        servlet.destroy();
    }

    private void writeResultMessage(ServletRequest servletRequest, ServletResponse servletResponse) throws IOException {
        TBinaryProtocol tBinaryProtocol = new TBinaryProtocol(
                new TIOStreamTransport(servletRequest.getInputStream(), servletResponse.getOutputStream())
        );
        try {
            tBinaryProtocol.writeMessageBegin(tBinaryProtocol.readMessageBegin());
            OwnerServiceSrv.getIntValue_result intValueResult = new OwnerServiceSrv.getIntValue_result();
            intValueResult.setSuccess(42);
            intValueResult.write(tBinaryProtocol);
            tBinaryProtocol.writeMessageEnd();
            tBinaryProtocol.getTransport().flush();
        } catch (TException ex) {
            throw new RuntimeException(ex);
        }
    }

}
