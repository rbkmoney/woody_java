package com.rbkmoney.woody.api.flow.error;

import com.rbkmoney.woody.api.trace.ContextSpan;

public interface WErrorMapper {

    WErrorDefinition mapToDef(Throwable t, ContextSpan contextSpan);

    Exception mapToError(WErrorDefinition eDefinition, ContextSpan contextSpan);

}
