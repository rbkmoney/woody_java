package com.rbkmoney.woody.api;

import com.rbkmoney.woody.api.trace.Span;
import org.slf4j.MDC;

/**
 * Created by tolkonepiu on 08.06.16.
 */
public class MDCUtils {

    public static final String SPAN_ID = "span_id";
    public static final String TRACE_ID = "trace_id";
    public static final String PARENT_ID = "parent_id";

    private MDCUtils() {
        throw new IllegalAccessError("Utility class");
    }

    /**
     * Put context ids in MDC
     *
     * @param span - service or client span
     */
    public static void putContextIds(Span span) {
        putContextIds(span.getId(), span.getTraceId(), span.getParentId());
    }

    /**
     * Put context ids in MDC
     *
     * @param spanId   - span id
     * @param traceId  - trace id
     * @param parentId - parent id
     */
    public static void putContextIds(String spanId, String traceId, String parentId) {
        MDC.put(SPAN_ID, spanId);
        MDC.put(TRACE_ID, traceId);
        MDC.put(PARENT_ID, parentId);
    }

    /**
     * Remove context ids from MDC
     */
    public static void removeContextIds() {
        MDC.remove(SPAN_ID);
        MDC.remove(TRACE_ID);
        MDC.remove(PARENT_ID);
    }

}
