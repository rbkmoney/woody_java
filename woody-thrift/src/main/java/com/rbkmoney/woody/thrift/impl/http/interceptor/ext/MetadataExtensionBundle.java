package com.rbkmoney.woody.thrift.impl.http.interceptor.ext;

import com.rbkmoney.woody.api.interceptor.ext.ExtensionBundle;
import com.rbkmoney.woody.api.interceptor.ext.InterceptorExtension;
import com.rbkmoney.woody.api.trace.Metadata;
import com.rbkmoney.woody.api.trace.context.metadata.MetadataConversionException;
import com.rbkmoney.woody.api.trace.context.metadata.MetadataExtensionKit;
import com.rbkmoney.woody.thrift.impl.http.interceptor.THRequestInterceptionException;
import com.rbkmoney.woody.thrift.impl.http.transport.THttpHeader;
import com.rbkmoney.woody.thrift.impl.http.transport.TTransportErrorType;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import static com.rbkmoney.woody.api.interceptor.ext.ExtensionBundle.ContextBundle.createCtxBundle;

/**
 * Created by vpankrashkin on 23.01.17.
 */
public class MetadataExtensionBundle extends ExtensionBundle {
    private static final Pattern KEY_PATTERN = Pattern.compile("[\\w-]{1,53}");

    public MetadataExtensionBundle(List<MetadataExtensionKit> extensionList) {
        super(createClientBundle(extensionList), createServiceBundle(extensionList));
    }

    private static ContextBundle createClientBundle(List<MetadataExtensionKit> extensionKits) {
        return createCtxBundle(
                        (InterceptorExtension<THCExtensionContext>) reqCCtx -> {
                            Metadata customMetadata = reqCCtx.getTraceData().getActiveSpan().getCustomMetadata();
                            Set<MetadataExtensionKit> unusedKits = new LinkedHashSet<>(extensionKits);
                            for (String key : customMetadata.getKeys()) {
                                if (KEY_PATTERN.matcher(key).matches()) {
                                    boolean applied = false;
                                    try {
                                        for (MetadataExtensionKit extKit : extensionKits) {
                                            if (applied |= extKit.getConverter().apply(key)) {
                                                unusedKits.remove(extKit);
                                                Object metaVal = extKit.getExtension().getValue(key, customMetadata);
                                                reqCCtx.setRequestHeader(formatHeaderKey(key), extKit.getConverter().convertToString(key, metaVal));
                                            }
                                        }
                                    } catch (MetadataConversionException e) {
                                        throw new THRequestInterceptionException(TTransportErrorType.BAD_HEADER, formatHeaderKey(key), e);
                                    }
                                    if (!applied) {
                                        reqCCtx.setRequestHeader(formatHeaderKey(key), String.valueOf(customMetadata.<Object>getValue(key)));
                                    }
                                } else {
                                    throw new THRequestInterceptionException(TTransportErrorType.BAD_HEADER, key);
                                }
                            }
                            for (MetadataExtensionKit extKit: unusedKits) {
                                if (!extKit.getConverter().applyToString()) {
                                    throw new THRequestInterceptionException(TTransportErrorType.BAD_HEADER, extKit.getConverter().getClass().getName() + " request not applied");
                                }
                            }
                        },
                        respCCtx -> {
                        }
                );
    }

    private static ContextBundle createServiceBundle(List<MetadataExtensionKit> extensionKits) {
        return createCtxBundle(
                        (InterceptorExtension<THSExtensionContext>) reqSCtx -> {
                            HttpServletRequest request = reqSCtx.getProviderRequest();
                            Set<MetadataExtensionKit> unusedKits = new LinkedHashSet<>(extensionKits);
                            Enumeration<String> headerKeys = request.getHeaderNames();
                            for (String headerKey; headerKeys.hasMoreElements();) {
                                headerKey = headerKeys.nextElement();
                                String metaKey = formatMetaKey(headerKey);
                                if (metaKey != null) {
                                    boolean applied = false;
                                    String metaStrVal = request.getHeader(headerKey);
                                    try {
                                        for (MetadataExtensionKit extKit : extensionKits) {
                                            if (applied |= extKit.getConverter().apply(metaKey)) {
                                                unusedKits.remove(extKit);
                                                Object metaVal = extKit.getConverter().convertToObject(metaKey, metaStrVal);
                                                extKit.getExtension().setValue(metaKey, metaVal, reqSCtx.getTraceData().getActiveSpan().getCustomMetadata());
                                            }
                                        }
                                    } catch (MetadataConversionException e) {
                                        throw new THRequestInterceptionException(TTransportErrorType.BAD_HEADER, headerKey, e);
                                    }
                                    if (!applied) {
                                        reqSCtx.getTraceData().getActiveSpan().getCustomMetadata().putValue(metaKey, metaStrVal);
                                    }
                                }
                            }
                            for (MetadataExtensionKit extKit: unusedKits) {
                                if (!extKit.getConverter().applyToString()) {
                                    throw new THRequestInterceptionException(TTransportErrorType.BAD_HEADER, extKit.getConverter().getClass().getName() + "response not applied");
                                }
                            }
                        },
                        respSCtx -> {
                        }
                );
    }

    private static String formatHeaderKey(String metaKey) {
        return THttpHeader.META.getKey() + metaKey.toLowerCase();
    }

    private static String formatMetaKey(String headerKey) {
        String keyPrefix = THttpHeader.META.getKey();
        return headerKey.startsWith(keyPrefix) ? headerKey.substring(keyPrefix.length()).toLowerCase() : null;
    }
}
