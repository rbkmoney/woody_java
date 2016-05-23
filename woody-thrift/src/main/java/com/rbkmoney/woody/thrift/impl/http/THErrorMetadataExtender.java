package com.rbkmoney.woody.thrift.impl.http;

import com.rbkmoney.woody.api.event.ErrorType;
import com.rbkmoney.woody.api.proxy.InstanceMethodCaller;
import com.rbkmoney.woody.api.proxy.MethodShadow;
import com.rbkmoney.woody.api.trace.*;
import com.rbkmoney.woody.thrift.impl.http.interceptor.THRequestInterceptionException;
import org.apache.thrift.TApplicationException;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TStruct;
import org.apache.thrift.transport.TTransportException;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

/**
 * Created by vpankrashkin on 29.04.16.
 */
public class THErrorMetadataExtender {
    private static final String UNKNOWN_ERROR_MESSAGE = "thrift application exception unknown";

    private final Map<Method, Class[]> errorsMap;

    public THErrorMetadataExtender(Class iface) {
        this.errorsMap = getDeclaredErrorsMap(iface);
    }

    public TraceData extendClientError(TraceData traceData) {

        extendError(traceData.getClientSpan(), (traceData1 -> {
            Metadata metadata = traceData.getClientSpan().getMetadata();
            Throwable callErr = ContextUtils.getCallError(traceData.getClientSpan());
            if (isWrappedError(callErr)) {
                extendWrappedErrorMetadata(metadata, callErr);
            } else {
                metadata.putValue(MetadataProperties.ERROR_TYPE, ErrorType.OTHER);
                metadata.putValue(MetadataProperties.ERROR_MESSAGE, UNKNOWN_ERROR_MESSAGE);
            }
            return traceData1;
        }));
        return traceData;
    }

    public TraceData extendServiceError(TraceData traceData) {
        extendError(traceData.getServiceSpan(), traceData1 -> {
            Metadata metadata = traceData.getServiceSpan().getMetadata();
            Throwable callErr = ContextUtils.getCallError(traceData.getServiceSpan());
            if (isWrappedError(callErr)) {
                extendWrappedErrorMetadata(metadata, callErr);
            } else if (callErr instanceof THRequestInterceptionException) {
                metadata.putValue(MetadataProperties.ERROR_TYPE, ErrorType.PROVIDER_ERROR);
                metadata.putValue(THMetadataProperties.TH_ERROR_TYPE, TErrorType.TRANSPORT);
                metadata.putValue(THMetadataProperties.TH_ERROR_SUBTYPE, ((THRequestInterceptionException) callErr).getErrorType());
                ContextUtils.setInterceptionErrorReason(traceData.getServiceSpan(), ((THRequestInterceptionException) callErr).getReason());
            } else {
                metadata.putValue(MetadataProperties.ERROR_TYPE, ErrorType.APPLICATION_UNKNOWN_ERROR);
                metadata.putValue(MetadataProperties.ERROR_MESSAGE, UNKNOWN_ERROR_MESSAGE);

            }
            return traceData1;
        });
        return traceData;
    }

    private void extendError(ContextSpan contextSpan, Function<ContextSpan, ContextSpan> undeclaredErrExtender) {
        Metadata metadata = contextSpan.getMetadata();
        Throwable callErr = ContextUtils.getCallError(contextSpan);
        if (callErr == null) {
            return;
        }

        InstanceMethodCaller caller = getCaller(metadata);
        if (caller == null) {
            return;
        }

        Throwable previousErr = ContextUtils.getMetadataParameter(contextSpan, Throwable.class, THMetadataProperties.TH_ERROR_METADATA_SOURCE);

        if (callErr == previousErr) {
            return;
        } else {
            contextSpan.getMetadata().removeValue(THMetadataProperties.TH_TRANSPORT_RESPONSE_SET);
        }
        metadata.putValue(MetadataProperties.ERROR_MESSAGE, callErr.getMessage());
        if (isDeclaredError(callErr.getClass(), caller.getTargetMethod())) {
            extendDeclaredErrorMetadata(metadata, callErr);
        } else {
            undeclaredErrExtender.apply(contextSpan);
        }
        return;
    }

    private void extendDeclaredErrorMetadata(Metadata metadata, Throwable err) {
        metadata.putValue(MetadataProperties.ERROR_TYPE, ErrorType.APPLICATION_KNOWN_ERROR);
        metadata.putValue(MetadataProperties.ERROR_NAME, getDeclaredErrName(err));
    }

    private void extendWrappedErrorMetadata(Metadata metadata, Throwable err) {
        ErrorType errorType;
        TErrorType tErrorType = null;
        String errMessage;
        if (err instanceof TApplicationException) {
            TApplicationException appError = (TApplicationException) err;

            switch (appError.getType()) {
                case TApplicationException.INTERNAL_ERROR:
                    errorType = ErrorType.APPLICATION_UNKNOWN_ERROR;
                    errMessage = UNKNOWN_ERROR_MESSAGE;
                    break;
                case TApplicationException.PROTOCOL_ERROR:
                    errorType = ErrorType.PROVIDER_ERROR;
                    tErrorType = TErrorType.PROTOCOL;
                    errMessage = err.getMessage();
                    break;
                case TApplicationException.UNKNOWN_METHOD:
                    errorType = ErrorType.PROVIDER_ERROR;
                    tErrorType = TErrorType.UNKNOWN_CALL;
                    errMessage = err.getMessage();
                    break;
                default:
                    errorType = ErrorType.PROVIDER_ERROR;
                    tErrorType = TErrorType.UNKNOWN;
                    errMessage = err.getMessage();
                    break;
            }
        } else if (err instanceof TTransportException) {
            TTransportException trError = (TTransportException) err;
            errorType = ErrorType.PROVIDER_ERROR;
            tErrorType = TErrorType.TRANSPORT;
            errMessage = trError.getMessage();
        } else {
            errorType = ErrorType.OTHER;
            errMessage = err.getMessage();
        }
        metadata.putValue(MetadataProperties.ERROR_TYPE, errorType);
        metadata.putValue(MetadataProperties.ERROR_MESSAGE, errMessage);
        if (tErrorType != null) {
            metadata.putValue(THMetadataProperties.TH_ERROR_TYPE, tErrorType);
        }
    }

    public boolean isDeclaredError(Class errClass, Method callMethod) {
        Class[] declaredErrors = errorsMap.get(callMethod);
        for (int i = 0; i < declaredErrors.length; ++i) {
            if (declaredErrors[i].isAssignableFrom(errClass)) {
                return true;
            }
        }
        return false;
    }

    private boolean isWrappedError(Throwable t) {
        return t instanceof TException;
    }

    private String getDeclaredErrName(Throwable t) {
        //TODO optimise this
        try {
            Field field = t.getClass().getDeclaredField("STRUCT_DESC");
            field.setAccessible(true);
            Object struct = field.get(t);
            if (struct instanceof TStruct) {
                return ((TStruct) struct).name;
            }
            return null;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return null;
        }
    }

    private InstanceMethodCaller getCaller(Metadata metadata) {
        Object callerObj = metadata.getValue(MetadataProperties.INSTANCE_METHOD_CALLER);
        return (callerObj instanceof InstanceMethodCaller) ? (InstanceMethodCaller) callerObj : null;
    }

    private Map<Method, Class[]> getDeclaredErrorsMap(Class iface) {
        Map<Method, Class[]> errorsMap = new TreeMap<>(MethodShadow.METHOD_COMPARATOR);
        Arrays.stream(iface.getMethods()).forEach(m ->
                errorsMap.put(m, Arrays.stream(m.getExceptionTypes())
                        .filter(e -> !e.getName().equals(TException.class.getName()))
                        .toArray(Class[]::new))
        );
        return errorsMap;
    }
}


