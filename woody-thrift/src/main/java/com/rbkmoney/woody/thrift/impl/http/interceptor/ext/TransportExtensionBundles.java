package com.rbkmoney.woody.thrift.impl.http.interceptor.ext;

import com.rbkmoney.woody.api.flow.error.WErrorDefinition;
import com.rbkmoney.woody.api.flow.error.WErrorType;
import com.rbkmoney.woody.api.interceptor.ext.ExtensionBundle;
import com.rbkmoney.woody.api.interceptor.ext.InterceptorExtension;
import com.rbkmoney.woody.api.trace.*;
import com.rbkmoney.woody.thrift.impl.http.THMetadataProperties;
import com.rbkmoney.woody.thrift.impl.http.THResponseInfo;
import com.rbkmoney.woody.thrift.impl.http.error.THProviderErrorMapper;
import com.rbkmoney.woody.thrift.impl.http.interceptor.THRequestInterceptionException;
import com.rbkmoney.woody.thrift.impl.http.transport.THttpHeader;
import com.rbkmoney.woody.thrift.impl.http.transport.TTransportErrorType;
import com.rbkmoney.woody.thrift.impl.http.transport.UrlStringEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.rbkmoney.woody.api.interceptor.ext.ExtensionBundle.ContextBundle.createCtxBundle;
import static com.rbkmoney.woody.api.interceptor.ext.ExtensionBundle.createExtBundle;
import static com.rbkmoney.woody.api.interceptor.ext.ExtensionBundle.createServiceExtBundle;
import static java.util.AbstractMap.SimpleEntry;

/**
 * Created by vpankrashkin on 14.12.16.
 */
public class TransportExtensionBundles {
    private static final Logger log = LoggerFactory.getLogger(TransportExtensionBundles.class);

    public static final ExtensionBundle TRANSPORT_CONFIG_BUNDLE = createServiceExtBundle(
            createCtxBundle(
                    (InterceptorExtension<THSExtensionContext>) reqSCtx -> {
                        String reqMethod = reqSCtx.getProviderRequest().getMethod();
                        if (!"POST".equals(reqMethod)) {
                            throw new THRequestInterceptionException(TTransportErrorType.BAD_REQUEST_TYPE, reqMethod);
                        }
                        String cType = reqSCtx.getProviderRequest().getContentType();
                        if (!"application/x-thrift".equalsIgnoreCase(cType)) {
                            throw new THRequestInterceptionException(TTransportErrorType.BAD_CONTENT_TYPE, cType);
                        }
                    },
                    respSCtx -> {
                    }
            )
    );

    public static final ExtensionBundle RPC_ID_BUNDLE = createExtBundle(
            createCtxBundle(
                    (InterceptorExtension<THCExtensionContext>) reqCCtx -> {
                        Span span = reqCCtx.getTraceData().getClientSpan().getSpan();
                        reqCCtx.setRequestHeader(THttpHeader.TRACE_ID.getKey(), span.getTraceId());
                        reqCCtx.setRequestHeader(THttpHeader.SPAN_ID.getKey(), span.getId());
                        reqCCtx.setRequestHeader(THttpHeader.PARENT_ID.getKey(), span.getParentId());
                    },
                    respCCtx -> {
                    }
            ),
            createCtxBundle(
                    (InterceptorExtension<THSExtensionContext>) reqSCtx -> {
                        HttpServletRequest request = reqSCtx.getProviderRequest();
                        Span span = reqSCtx.getTraceData().getServiceSpan().getSpan();
                        Stream.of(
                                new SimpleEntry<>(THttpHeader.TRACE_ID, (Consumer<String>) (id) -> span.setTraceId(id)),
                                new SimpleEntry<>(THttpHeader.PARENT_ID, (Consumer<String>) (id) -> span.setParentId(id)),
                                new SimpleEntry<>(THttpHeader.SPAN_ID, (Consumer<String>) (id) -> span.setId(id))
                        )
                                .filter(entry -> {
                                    String id = Optional.ofNullable(request.getHeader(entry.getKey().getKey())).orElse("");
                                    if (id.isEmpty()) {
                                        return true;
                                    } else {
                                        entry.getValue().accept(id);
                                        return false;
                                    }
                                })
                                .findFirst()
                                .ifPresent(entry -> {
                                    throw new THRequestInterceptionException(TTransportErrorType.BAD_TRACE_HEADER, entry.getKey().getKey());
                                });
                    },
                    (InterceptorExtension<THSExtensionContext>) respSCtx -> {
                        Span span = respSCtx.getTraceData().getServiceSpan().getSpan();
                        respSCtx.setResponseHeader(THttpHeader.TRACE_ID.getKey(), span.getTraceId());
                        respSCtx.setResponseHeader(THttpHeader.PARENT_ID.getKey(), span.getParentId());
                        respSCtx.setResponseHeader(THttpHeader.SPAN_ID.getKey(), span.getId());
                    }
            )
    );

    public static final ExtensionBundle CALL_ENDPOINT_BUNDLE = createExtBundle(
            createCtxBundle(
                    (InterceptorExtension<THCExtensionContext>) reqCCtx -> {
                        ContextSpan contextSpan = reqCCtx.getTraceData().getClientSpan();
                        URL url = reqCCtx.getRequestCallEndpoint();
                        contextSpan.getMetadata().putValue(MetadataProperties.CALL_ENDPOINT, new UrlStringEndpoint(url == null ? null : url.toString()));
                    },
                    respCCtx -> {
                    }
            ),
            createCtxBundle(
                    (InterceptorExtension<THSExtensionContext>) reqSCtx -> {
                        HttpServletRequest request = reqSCtx.getProviderRequest();
                        String queryString = request.getQueryString();
                        StringBuffer sb = request.getRequestURL();
                        if (queryString != null) {
                            sb.append('?').append(request.getQueryString());
                        }
                        reqSCtx.getTraceData().getServiceSpan().getMetadata().putValue(MetadataProperties.CALL_ENDPOINT, new UrlStringEndpoint(sb.toString()));
                    },
                    reqSCtx -> {
                    }
            )
    );

    public static final ExtensionBundle TRANSPORT_INJECTION_BUNDLE = createExtBundle(
            createCtxBundle(
                    (InterceptorExtension<THCExtensionContext>) reqCCtx -> {
                        reqCCtx.getTraceData().getClientSpan().getMetadata().putValue(THMetadataProperties.TH_TRANSPORT_REQUEST, reqCCtx.getProviderContext());
                    },
                    (InterceptorExtension<THCExtensionContext>) respCCtx -> {
                        respCCtx.getTraceData().getClientSpan().getMetadata().putValue(THMetadataProperties.TH_TRANSPORT_RESPONSE, respCCtx.getProviderContext());
                    }
            ),
            createCtxBundle(
                    (InterceptorExtension<THSExtensionContext>) reqSCtx -> {
                        HttpServletResponse response = ContextUtils.getContextValue(HttpServletResponse.class, reqSCtx.getContextParameters(), 0);
                        ServiceSpan serviceSpan = reqSCtx.getTraceData().getServiceSpan();
                        serviceSpan.getMetadata().putValue(THMetadataProperties.TH_TRANSPORT_REQUEST, reqSCtx.getProviderRequest());
                        serviceSpan.getMetadata().putValue(THMetadataProperties.TH_TRANSPORT_RESPONSE, response);
                    },
                    respSCtx -> {
                    }
            )
    );

    public static final ExtensionBundle TRANSPORT_STATE_MAPPING_BUNDLE = createExtBundle(
            createCtxBundle(
                    reqCCtx -> {
                    },
                    (InterceptorExtension<THCExtensionContext>) respCCtx -> {
                        int status = respCCtx.getResponseStatus();
                        Metadata metadata = respCCtx.getTraceData().getClientSpan().getMetadata();
                        metadata.putValue(THMetadataProperties.TH_RESPONSE_STATUS, status);
                        metadata.putValue(THMetadataProperties.TH_RESPONSE_MESSAGE, respCCtx.getResponseMessage());

                        WErrorDefinition errorDefinition = THProviderErrorMapper.createErrorDefinition(new THResponseInfo(status, respCCtx.getResponseHeader(THttpHeader.ERROR_CLASS.getKey()), respCCtx.getResponseHeader(THttpHeader.ERROR_REASON.getKey()), respCCtx.getResponseMessage()), () -> {
                            throw new THRequestInterceptionException(TTransportErrorType.BAD_HEADER, THttpHeader.ERROR_CLASS.getKey());
                        });

                        metadata.putValue(MetadataProperties.ERROR_DEFINITION, errorDefinition);
                        if (errorDefinition != null && errorDefinition.getErrorType() != WErrorType.BUSINESS_ERROR) {
                            metadata.putValue(MetadataProperties.RESPONSE_SKIP_READING_FLAG, true);
                        }
                    }
            ),
            createCtxBundle(
                    reqSCtx -> {
                    },
                    (InterceptorExtension<THSExtensionContext>) respSCtx -> {
                        ContextSpan serviceSpan = respSCtx.getTraceData().getServiceSpan();
                        if (serviceSpan.getMetadata().containsKey(THMetadataProperties.TH_TRANSPORT_RESPONSE_SET_FLAG)) {
                            return;
                        }
                        logIfError(serviceSpan);
                        HttpServletResponse response = respSCtx.getProviderResponse();
                        if (response.isCommitted()) {
                            log.error("Can't perform response mapping: Transport response is already committed");
                        } else {
                            THResponseInfo responseInfo = THProviderErrorMapper.getResponseInfo(serviceSpan);
                            response.setStatus(responseInfo.getStatus());
                            Optional.ofNullable(responseInfo.getErrClass()).ifPresent(val -> response.setHeader(THttpHeader.ERROR_CLASS.getKey(), val));
                            Optional.ofNullable(responseInfo.getErrReason()).ifPresent(val -> response.setHeader(THttpHeader.ERROR_REASON.getKey(), val));
                            serviceSpan.getMetadata().putValue(THMetadataProperties.TH_TRANSPORT_RESPONSE_SET_FLAG, true);
                        }
                    }
            )
    );

    private static final List<ExtensionBundle> clientList = Collections.unmodifiableList(Arrays.asList(
            RPC_ID_BUNDLE,
            CALL_ENDPOINT_BUNDLE,
            TRANSPORT_STATE_MAPPING_BUNDLE,
            TRANSPORT_INJECTION_BUNDLE
    ));

    private static final List<ExtensionBundle> serviceList = Collections.unmodifiableList(Arrays.asList(
            TRANSPORT_CONFIG_BUNDLE,
            RPC_ID_BUNDLE,
            CALL_ENDPOINT_BUNDLE,
            TRANSPORT_STATE_MAPPING_BUNDLE,
            TRANSPORT_INJECTION_BUNDLE
    ));

    public static List<ExtensionBundle> getClientExtensions() {
        return clientList;
    }

    public static List<ExtensionBundle> getServiceExtensions() {
        return serviceList;
    }

    public static List<ExtensionBundle> getExtensions(boolean isClient) {
        return isClient ? getClientExtensions() : getServiceExtensions();
    }

    private static void logIfError(ContextSpan contextSpan) {
        Throwable t = ContextUtils.getCallError(contextSpan);
        if (t != null) {
            log.debug("Response has error:", t);
        }
    }
}