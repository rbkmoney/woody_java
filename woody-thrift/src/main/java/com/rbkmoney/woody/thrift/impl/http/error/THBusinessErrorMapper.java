package com.rbkmoney.woody.thrift.impl.http.error;

import com.rbkmoney.woody.api.flow.error.WErrorDefinition;
import com.rbkmoney.woody.api.flow.error.WErrorMapper;
import com.rbkmoney.woody.api.flow.error.WErrorSource;
import com.rbkmoney.woody.api.flow.error.WErrorType;
import com.rbkmoney.woody.api.proxy.InstanceMethodCaller;
import com.rbkmoney.woody.api.proxy.MethodShadow;
import com.rbkmoney.woody.api.trace.ContextSpan;
import com.rbkmoney.woody.api.trace.ContextUtils;
import com.rbkmoney.woody.api.trace.Metadata;
import com.rbkmoney.woody.api.trace.MetadataProperties;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TStruct;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public class THBusinessErrorMapper implements WErrorMapper {
    public static final UnaryOperator<String> BUSINESS_ERROR_REASON_FUNC = errName -> errName;

    private final Map<Method, Class[]> errorsMap;

    private Map<Method, Class[]> getDeclaredErrorsMap(Class iface) {
        Map<Method, Class[]> errorsMap = new TreeMap<>(MethodShadow.METHOD_COMPARATOR);
        Arrays.stream(iface.getMethods()).forEach(m ->
                errorsMap.put(m, Arrays.stream(m.getExceptionTypes())
                        .filter(e -> !e.getName().equals(TException.class.getName()))
                        .toArray(Class[]::new))
        );
        return errorsMap;
    }

    public THBusinessErrorMapper(Class iface) {
        this.errorsMap = getDeclaredErrorsMap(iface);
    }

    @Override
    public WErrorDefinition mapToDef(Throwable t, ContextSpan contextSpan) {
        InstanceMethodCaller caller = getCaller(contextSpan.getMetadata());
        if (caller == null) {
            return null;
        }

        if (isDeclaredError(t.getClass(), caller.getTargetMethod())) {
            WErrorDefinition errorDefinition = ContextUtils.getErrorDefinition(contextSpan);
            String errName = getDeclaredErrName(t);
            if (!(errorDefinition != null &&
                    errorDefinition.getErrorType() == WErrorType.BUSINESS_ERROR && Objects.equals(
                    errorDefinition.getErrorName(), errName))) {
                errorDefinition = new WErrorDefinition(WErrorSource.INTERNAL);
                errorDefinition.setErrorType(WErrorType.BUSINESS_ERROR);
                errorDefinition.setErrorSource(WErrorSource.INTERNAL);
                errorDefinition.setErrorReason(BUSINESS_ERROR_REASON_FUNC.apply(errName));
                errorDefinition.setErrorName(errName);
                errorDefinition.setErrorMessage(t.getMessage());
            }
            return errorDefinition;
        }
        return null;
    }

    @Override
    public Exception mapToError(WErrorDefinition eDefinition, ContextSpan contextSpan) {
        return null;//business error has already been thrown, no need in overriding
    }

    private boolean isDeclaredError(Class errClass, Method callMethod) {
        Class[] declaredErrors = errorsMap.get(callMethod);
        if (declaredErrors != null) {
            for (int i = 0; i < declaredErrors.length; ++i) {
                if (declaredErrors[i].isAssignableFrom(errClass)) {
                    return true;
                }
            }
        }
        return false;
    }

    private InstanceMethodCaller getCaller(Metadata metadata) {
        Object callerObj = metadata.getValue(MetadataProperties.INSTANCE_METHOD_CALLER);
        return (callerObj instanceof InstanceMethodCaller) ? (InstanceMethodCaller) callerObj : null;
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


}
