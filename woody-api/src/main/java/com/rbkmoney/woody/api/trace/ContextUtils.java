package com.rbkmoney.woody.api.trace;

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
        return getMetadataParameter(span, Throwable.class, MetadataProperties.INTERCEPTION_ERROR);
    }

    public static void setInterceptionError(ContextSpan span, Throwable t) {
        span.getMetadata().putValue(MetadataProperties.INTERCEPTION_ERROR, t);
    }

    public static void setInterceptionErrorReason(ContextSpan span, Object reason) {
        span.getMetadata().putValue(MetadataProperties.INTERCEPTION_ERROR_REASON, reason);
    }

    public static <T> T getInterceptionErrorReason(ContextSpan span, Class<T> targetType) {
        return getMetadataParameter(span, targetType, MetadataProperties.INTERCEPTION_ERROR_REASON);
    }

    public static void setCallError(ContextSpan span, Throwable t) {
        span.getMetadata().putValue(MetadataProperties.CALL_ERROR, t);
    }

    public static Throwable getCallError(ContextSpan span) {
        return getMetadataParameter(span, Throwable.class, MetadataProperties.CALL_ERROR);
    }

    public static boolean hasCallErrors(ContextSpan span) {
        return span.getMetadata().containsKey(MetadataProperties.CALL_ERROR);
    }

    public static void tryThrowInterceptionError(ContextSpan span) throws Throwable {
        Throwable t = getInterceptionError(span);
        if (t != null) {
            throw t;
        }
    }

    public static <T> T getMetadataParameter(ContextSpan span, Class<T> targetType, String key) {
        Object obj = span.getMetadata().getValue(key);
        if (obj == null) {
            return null;
        } else if (targetType.isAssignableFrom(obj.getClass())) {
            return (T) obj;
        }
        return null;
    }

    public static <T> T getContextParameter(Class<T> targetType, Object[] params, int index) {
        if (params == null || params.length <= index || params[index] == null) {
            return null;
        }

        if (targetType.isAssignableFrom(params[index].getClass())) {
            return (T) params[index];
        }
        return null;
    }
}
