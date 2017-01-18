package com.rbkmoney.woody.api.flow.error;

import com.rbkmoney.woody.api.trace.ContextSpan;

/**
 * Created by vpankrashkin on 12.12.16.
 */
public interface WErrorMapper {

    WErrorDefinition mapToDef(Throwable t, ContextSpan contextSpan);

    Exception mapToError(WErrorDefinition eDefinition, ContextSpan contextSpan);

}
