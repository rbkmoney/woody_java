package com.rbkmoney.woody.thrift.impl.http;

import com.rbkmoney.woody.api.flow.WFlow;
import com.rbkmoney.woody.api.flow.error.WErrorDefinition;
import com.rbkmoney.woody.api.flow.error.WErrorSource;
import com.rbkmoney.woody.api.flow.error.WErrorType;
import com.rbkmoney.woody.api.flow.error.WRuntimeException;
import com.rbkmoney.woody.api.trace.ContextUtils;
import com.rbkmoney.woody.api.trace.Metadata;
import com.rbkmoney.woody.api.trace.context.metadata.MetadataConversionException;
import com.rbkmoney.woody.api.trace.context.metadata.MetadataConverter;
import com.rbkmoney.woody.api.trace.context.metadata.MetadataExtension;
import com.rbkmoney.woody.api.trace.context.metadata.MetadataExtensionKit;
import com.rbkmoney.woody.rpc.Owner;
import com.rbkmoney.woody.rpc.OwnerServiceSrv;
import com.rbkmoney.woody.rpc.test_error;
import com.rbkmoney.woody.thrift.impl.http.transport.THttpHeader;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TIOStreamTransport;
import org.junit.Test;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Map;

import static org.junit.Assert.*;

public class TestClientAndServerHttpHeaders extends AbstractTest {

    private final String servletContextPath = "/test_servlet";

    private final Servlet testServlet = createThriftRPCService(OwnerServiceSrv.Iface.class, new OwnerServiceSrv.Iface() {

        @Override
        public int getIntValue() throws TException {
            return 42;
        }

        @Override
        public Owner getOwner(int id) throws TException {
            return new Owner(id, "test");
        }

        @Override
        public Owner getErrOwner(int id) throws test_error, TException {
            throw new test_error();
        }

        @Override
        public void setOwner(Owner owner) throws TException {
            //nothing
        }

        @Override
        public void setOwnerOneway(Owner owner) throws TException {
            //nothing
        }

        @Override
        public Owner setErrOwner(Owner owner, int id) throws test_error, TException {
            throw new test_error();
        }
    });

    @Test
    public void testCheckClientTraceHeaders() throws TException {
        addServlet(new HttpServlet() {
            @Override
            protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
                for (THttpHeader tHttpHeader : Arrays.asList(THttpHeader.SPAN_ID, THttpHeader.TRACE_ID, THttpHeader.PARENT_ID)) {
                    assertNotNull(request.getHeader(tHttpHeader.getKey()));
                    assertEquals(request.getHeader(tHttpHeader.getKey()), request.getHeader(tHttpHeader.getOldKey()));
                }
                writeResultMessage(request, response);
            }
        }, "/check_trace_headers");
        OwnerServiceSrv.Iface client = createThriftRPCClient(OwnerServiceSrv.Iface.class, getUrlString("/check_trace_headers"));
        client.getIntValue();
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

        addServlet(new HttpServlet() {
            @Override
            protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
                assertEquals(request.getHeader(THttpHeader.META.getKey() + metadataWithExtensionKit.getKey()), metadataWithExtensionKit.getValue());
                assertEquals(request.getHeader(THttpHeader.META.getOldKey() + metadataWithExtensionKit.getKey()), metadataWithExtensionKit.getValue());

                assertEquals(request.getHeader(THttpHeader.META.getKey() + metadataWithoutExtensionKit.getKey()), metadataWithoutExtensionKit.getValue());
                assertEquals(request.getHeader(THttpHeader.META.getOldKey() + metadataWithoutExtensionKit.getKey()), metadataWithoutExtensionKit.getValue());

                writeResultMessage(request, response);
            }
        }, "/check_meta_headers");
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
    }

    @Test
    public void testWhenTraceDataIsEmpty() throws TException {
        addServlet(testServlet, servletContextPath);
        CloseableHttpClient httpClient = HttpClients.custom()
                .addInterceptorFirst((HttpRequestInterceptor) (httpRequest, httpContext) -> {
                    httpRequest.removeHeader(httpRequest.getFirstHeader(THttpHeader.SPAN_ID.getKey()));
                    httpRequest.removeHeader(httpRequest.getFirstHeader(THttpHeader.SPAN_ID.getOldKey()));
                    httpRequest.removeHeader(httpRequest.getFirstHeader(THttpHeader.TRACE_ID.getKey()));
                    httpRequest.removeHeader(httpRequest.getFirstHeader(THttpHeader.TRACE_ID.getOldKey()));
                    httpRequest.removeHeader(httpRequest.getFirstHeader(THttpHeader.PARENT_ID.getKey()));
                    httpRequest.removeHeader(httpRequest.getFirstHeader(THttpHeader.PARENT_ID.getOldKey()));
                }).build();
        OwnerServiceSrv.Iface client = createThriftRPCClient(OwnerServiceSrv.Iface.class, getUrlString(servletContextPath), 5000, httpClient);
        try {
            client.getIntValue();
            fail();
        } catch (WRuntimeException ex) {
            WErrorDefinition errorDefinition = ex.getErrorDefinition();
            assertEquals(WErrorSource.EXTERNAL, errorDefinition.getGenerationSource());
            assertEquals(WErrorType.UNEXPECTED_ERROR, errorDefinition.getErrorType());
            assertEquals(WErrorSource.INTERNAL, errorDefinition.getErrorSource());
            assertEquals("Bad Request", errorDefinition.getErrorMessage());
            assertEquals("x-rbk-trace-id, x-rbk-parent-id, x-rbk-span-id missing", errorDefinition.getErrorReason());
        }
    }

    @Test
    public void testWhenTraceDataWithOldHeaders() throws TException {
        addServlet(testServlet, servletContextPath);
        CloseableHttpClient httpClient = HttpClients.custom()
                .addInterceptorFirst((HttpRequestInterceptor) (httpRequest, httpContext) -> {
                    httpRequest.removeHeader(httpRequest.getFirstHeader(THttpHeader.SPAN_ID.getKey()));
                    httpRequest.removeHeader(httpRequest.getFirstHeader(THttpHeader.TRACE_ID.getKey()));
                    httpRequest.removeHeader(httpRequest.getFirstHeader(THttpHeader.PARENT_ID.getKey()));
                })
                .addInterceptorLast((HttpResponseInterceptor) (httpResponse, httpContext) -> {
                            assertTrue(httpResponse.containsHeader(THttpHeader.SPAN_ID.getKey()));
                            assertEquals(httpResponse.getLastHeader(THttpHeader.SPAN_ID.getKey()).getValue(), httpResponse.getLastHeader(THttpHeader.SPAN_ID.getOldKey()).getValue());
                            assertTrue(httpResponse.containsHeader(THttpHeader.TRACE_ID.getKey()));
                            assertEquals(httpResponse.getLastHeader(THttpHeader.TRACE_ID.getKey()).getValue(), httpResponse.getLastHeader(THttpHeader.TRACE_ID.getOldKey()).getValue());
                            assertTrue(httpResponse.containsHeader(THttpHeader.PARENT_ID.getKey()));
                            assertEquals(httpResponse.getLastHeader(THttpHeader.PARENT_ID.getKey()).getValue(), httpResponse.getLastHeader(THttpHeader.PARENT_ID.getOldKey()).getValue());
                        }
                ).build();
        OwnerServiceSrv.Iface client = createThriftRPCClient(OwnerServiceSrv.Iface.class, getUrlString(servletContextPath), 5000, httpClient);
        client.getIntValue();
    }

    @Test
    public void testWhenTraceDataWithNewHeaders() throws TException {
        addServlet(testServlet, servletContextPath);
        CloseableHttpClient httpClient = HttpClients.custom()
                .addInterceptorFirst((HttpRequestInterceptor) (httpRequest, httpContext) -> {
                    httpRequest.removeHeader(httpRequest.getFirstHeader(THttpHeader.SPAN_ID.getOldKey()));
                    httpRequest.removeHeader(httpRequest.getFirstHeader(THttpHeader.TRACE_ID.getOldKey()));
                    httpRequest.removeHeader(httpRequest.getFirstHeader(THttpHeader.PARENT_ID.getOldKey()));
                })
                .addInterceptorLast((HttpResponseInterceptor) (httpResponse, httpContext) -> {
                            assertTrue(httpResponse.containsHeader(THttpHeader.SPAN_ID.getOldKey()));
                            assertEquals(httpResponse.getLastHeader(THttpHeader.SPAN_ID.getOldKey()).getValue(), httpResponse.getLastHeader(THttpHeader.SPAN_ID.getKey()).getValue());
                            assertTrue(httpResponse.containsHeader(THttpHeader.TRACE_ID.getOldKey()));
                            assertEquals(httpResponse.getLastHeader(THttpHeader.TRACE_ID.getKey()).getValue(), httpResponse.getLastHeader(THttpHeader.TRACE_ID.getOldKey()).getValue());
                            assertTrue(httpResponse.containsHeader(THttpHeader.PARENT_ID.getOldKey()));
                            assertEquals(httpResponse.getLastHeader(THttpHeader.PARENT_ID.getKey()).getValue(), httpResponse.getLastHeader(THttpHeader.PARENT_ID.getOldKey()).getValue());
                        }
                ).build();
        OwnerServiceSrv.Iface client = createThriftRPCClient(OwnerServiceSrv.Iface.class, getUrlString(servletContextPath), 5000, httpClient);
        client.getIntValue();
    }

    @Test
    public void testWhenThrowError() {
        addServlet(testServlet, servletContextPath);
        CloseableHttpClient httpClient = HttpClients.custom()
                .addInterceptorFirst((HttpResponseInterceptor) (httpResponse, httpContext) -> {
                            assertTrue(httpResponse.containsHeader(THttpHeader.ERROR_CLASS.getOldKey()));
                            assertEquals(httpResponse.getLastHeader(THttpHeader.ERROR_CLASS.getOldKey()).getValue(), httpResponse.getLastHeader(THttpHeader.ERROR_CLASS.getKey()).getValue());
                            assertTrue(httpResponse.containsHeader(THttpHeader.ERROR_REASON.getOldKey()));
                            assertEquals(httpResponse.getLastHeader(THttpHeader.ERROR_REASON.getOldKey()).getValue(), httpResponse.getLastHeader(THttpHeader.ERROR_REASON.getKey()).getValue());
                        }
                ).build();
        OwnerServiceSrv.Iface client = createThriftRPCClient(OwnerServiceSrv.Iface.class, getUrlString(servletContextPath), 5000, httpClient);
        try {
            client.getErrOwner(1);
            fail();
        } catch (TException ex) {

        }
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
