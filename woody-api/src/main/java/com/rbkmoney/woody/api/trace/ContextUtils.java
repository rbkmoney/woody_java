package com.rbkmoney.woody.api.trace;

import com.rbkmoney.woody.api.flow.error.WErrorDefinition;
import com.rbkmoney.woody.api.trace.context.TraceContext;
import com.rbkmoney.woody.api.trace.context.metadata.MetadataExtension;

import java.time.Instant;
import java.util.function.Function;

/**
 * Created by vpankrashkin on 11.05.16.
 */
public class ContextUtils {
    public static <T> T createErrIfNotIntercepted(ContextSpan span, Function<Throwable, T> errConstructor) {
        Throwable err = getInterceptionError(span);
        if (err != null) {
            return errConstructor.apply(err);
        }
        return null;
    }

    public static Throwable getInterceptionError(ContextSpan span) {
        return getMetadataValue(span, Throwable.class, MetadataProperties.INTERCEPTION_ERROR);
    }

    public static void setInterceptionError(ContextSpan span, Throwable t) {
        span.getMetadata().putValue(MetadataProperties.INTERCEPTION_ERROR, t);
    }

    public static void setInterceptionErrorReason(ContextSpan span, Object reason) {
        span.getMetadata().putValue(MetadataProperties.INTERCEPTION_ERROR_REASON, reason);
    }

    public static <T> T getInterceptionErrorReason(ContextSpan span, Class<T> targetType) {
        return getMetadataValue(span, targetType, MetadataProperties.INTERCEPTION_ERROR_REASON);
    }

    public static void setCallError(ContextSpan span, Throwable t) {
        span.getMetadata().putValue(MetadataProperties.CALL_ERROR, t);
    }

    public static Throwable getCallError(ContextSpan span) {
        return getMetadataValue(span, Throwable.class, MetadataProperties.CALL_ERROR);
    }

    public static boolean hasCallErrors(ContextSpan span) {
        return span.getMetadata().containsKey(MetadataProperties.CALL_ERROR);
    }

    public static WErrorDefinition getErrorDefinition(ContextSpan span) {
        return span.getMetadata().getValue(MetadataProperties.ERROR_DEFINITION);
    }

    public static Instant getDeadline(ContextSpan contextSpan) {
        Span span = contextSpan.getSpan();
        if (span.hasDeadline()) {
            return Instant.ofEpochMilli(span.getDeadline());
        }
        return null;
    }

    public static void setDeadline(Instant deadline) {
        setDeadline(TraceContext.getCurrentTraceData().getClientSpan(), deadline);
    }

    public static void setDeadline(ContextSpan span, Instant deadline) {
        if (deadline != null) {
            span.getSpan().setDeadline(deadline.toEpochMilli());
        }
    }

    /**
     * @param span context with current deadline
     * @param defaultTimeout default timeout
     * @return return 0 if deadline <= 0, else return diff deadline - currentTime
     */
    public static int getExecutionTimeout(ContextSpan span, int defaultTimeout) {
        Instant deadline = getDeadline(span);
        if (deadline != null) {
            int executionTimeout = Math.toIntExact(deadline.toEpochMilli() - System.currentTimeMillis());
            return executionTimeout > 0 ? executionTimeout : 0;
        }
        return defaultTimeout;
    }

    public static void tryThrowInterceptionError(ContextSpan span) throws Throwable {
        Throwable t = getInterceptionError(span);
        if (t != null) {
            throw t;
        }
    }

    public static <T> T getCustomMetadataValue(MetadataExtension<T> extension) {
        return extension.getValue(TraceContext.getCurrentTraceData().getActiveSpan().getCustomMetadata());
    }

    public static <T> T getCustomMetadataValue(String key, MetadataExtension<T> extension) {
        return extension.getValue(key, TraceContext.getCurrentTraceData().getActiveSpan().getCustomMetadata());
    }

    public static <T, TT extends T> void setCustomMetadataValue(TT val, MetadataExtension<T> extension) {
        extension.setValue(val, TraceContext.getCurrentTraceData().getActiveSpan().getCustomMetadata());
    }

    public static <T, TT extends T> void setCustomMetadataValue(String key, TT val, MetadataExtension<T> extension) {
        extension.setValue(key, val, TraceContext.getCurrentTraceData().getActiveSpan().getCustomMetadata());
    }

    public static Object setCustomMetadataValue(String key, Object val) {
        return TraceContext.getCurrentTraceData().getActiveSpan().getCustomMetadata().putValue(key, val);
    }

    public static <T> T getMetadataValue(Class<T> targetType, String key) {
        return getMetadataValue(TraceContext.getCurrentTraceData().getActiveSpan(), targetType, key);
    }

    public static <T> T getCustomMetadataValue(Class<T> targetType, String key) {
        return getCustomMetadataValue(TraceContext.getCurrentTraceData().getActiveSpan(), targetType, key);
    }

    public static <T> T getMetadataValue(ContextSpan span, Class<T> targetType, String key) {
        return getMetadataValue(span.getMetadata(), targetType, key);
    }

    public static <T> T getCustomMetadataValue(ContextSpan span, Class<T> targetType, String key) {
        return getMetadataValue(span.getCustomMetadata(), targetType, key);
    }

    public static <T> T getMetadataValue(Metadata metadata, Class<T> targetType, String key) {
        Object obj = metadata.getValue(key);
        if (obj == null) {
            return null;
        } else if (targetType.isAssignableFrom(obj.getClass())) {
            return (T) obj;
        }
        return null;
    }

    public static <T> T getContextValue(Class<T> targetType, Object[] params, int index) {
        if (params == null || params.length <= index || params[index] == null) {
            return null;
        }

        if (targetType.isAssignableFrom(params[index].getClass())) {
            return (T) params[index];
        }
        return null;
    }
}
