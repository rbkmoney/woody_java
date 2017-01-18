package com.rbkmoney.woody.thrift.impl.http.interceptor.ext;

import com.rbkmoney.woody.api.event.CallType;
import com.rbkmoney.woody.api.interceptor.ext.ExtensionBundle;
import com.rbkmoney.woody.api.trace.Metadata;
import com.rbkmoney.woody.api.trace.MetadataProperties;
import com.rbkmoney.woody.thrift.impl.http.THMetadataProperties;
import org.apache.thrift.protocol.TMessage;
import org.apache.thrift.protocol.TMessageType;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.rbkmoney.woody.api.interceptor.ext.ExtensionBundle.ContextBundle.createCtxBundle;
import static com.rbkmoney.woody.api.interceptor.ext.ExtensionBundle.createExtBundle;


/**
 * Created by vpankrashkin on 14.12.16.
 */
public class MessageExtensionBundles {
    public static final ExtensionBundle CALL_INFO_INJECTION_BUNDLE = createExtBundle(
            createCtxBundle(
                    reqCtx -> {
                        TMessage tMessage = (TMessage) reqCtx.getProviderContext();
                        Metadata metadata = reqCtx.getTraceData().getClientSpan().getMetadata();
                        metadata.putValue(MetadataProperties.CALL_NAME, tMessage.name);
                        metadata.putValue(MetadataProperties.CALL_TYPE, tMessage.type == TMessageType.ONEWAY ? CallType.CAST : CallType.CALL);
                        metadata.putValue(THMetadataProperties.TH_CALL_MSG_TYPE, tMessage.type);
                    },
                    respCtx -> {
                        TMessage tMessage = (TMessage) respCtx.getProviderContext();
                        Metadata metadata = respCtx.getTraceData().getClientSpan().getMetadata();
                        metadata.putValue(THMetadataProperties.TH_CALL_RESULT_MSG_TYPE, tMessage.type);
                        metadata.putValue(MetadataProperties.CALL_REQUEST_PROCESSED_FLAG, true );
                    }
            )
    );

    private static final List<ExtensionBundle> bundleList = Collections.unmodifiableList(Arrays.asList(
            CALL_INFO_INJECTION_BUNDLE
    ));

    public static List<ExtensionBundle> getClientExtensions() {
        return bundleList;
    }

    public static List<ExtensionBundle> getServiceExtensions() {
        return bundleList;
    }

    public static List<ExtensionBundle> getExtensions(boolean isClient) {
        return isClient ? getClientExtensions() : getServiceExtensions();
    }

}
