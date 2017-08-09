package com.rbkmoney.woody.thrift.impl.http.error;

import com.rbkmoney.woody.api.flow.error.WErrorDefinition;
import com.rbkmoney.woody.api.flow.error.WErrorMapper;
import com.rbkmoney.woody.api.flow.error.WErrorSource;
import com.rbkmoney.woody.api.flow.error.WErrorType;
import com.rbkmoney.woody.api.trace.ContextSpan;
import org.apache.http.NoHttpResponseException;

import java.net.SocketTimeoutException;
import java.util.regex.Pattern;

public class THTransportErrorMapper implements WErrorMapper {
    private static final ErrorAnalyzer[] analyzers = new ErrorAnalyzer[]{
            new ErrorAnalyzer(Pattern.compile(SocketTimeoutException.class.getName()), (t, c) -> {
                WErrorDefinition def = new WErrorDefinition(WErrorSource.EXTERNAL);
                def.setErrorSource(WErrorSource.INTERNAL);
                def.setErrorType(WErrorType.UNDEFINED_RESULT);
                def.setErrorReason(t.toString());
                def.setErrorMessage(t.getMessage());
                return def;
            }),
            new ErrorAnalyzer(Pattern.compile("java.net.Socket.*"), THTransportErrorMapper::genUnavailableResult),
            new ErrorAnalyzer(Pattern.compile(NoHttpResponseException.class.getName()), THTransportErrorMapper::genUnavailableResult),
    };

    private static WErrorDefinition genUnavailableResult(Throwable t, ContextSpan c) {
        WErrorDefinition def = new WErrorDefinition(WErrorSource.EXTERNAL);
        def.setErrorSource(WErrorSource.INTERNAL);
        def.setErrorType(WErrorType.UNAVAILABLE_RESULT);
        def.setErrorReason(t.toString());
        def.setErrorMessage(t.getMessage());
        return def;
    }

    @Override
    public WErrorDefinition mapToDef(Throwable t, ContextSpan contextSpan) {
        return ErrorStackAnalyzer.analyzeStack(analyzers, t, contextSpan);
    }

    @Override
    public Exception mapToError(WErrorDefinition eDefinition, ContextSpan contextSpan) {
        return null;
    }

}