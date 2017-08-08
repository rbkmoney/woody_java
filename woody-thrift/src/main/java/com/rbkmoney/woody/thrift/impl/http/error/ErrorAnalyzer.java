package com.rbkmoney.woody.thrift.impl.http.error;

import com.rbkmoney.woody.api.flow.error.WErrorDefinition;
import com.rbkmoney.woody.api.trace.ContextSpan;

import java.util.function.BiFunction;
import java.util.regex.Pattern;

class ErrorAnalyzer {
    private final Pattern pattern;
    private final BiFunction<Throwable, ContextSpan, WErrorDefinition> analyzer;

    public ErrorAnalyzer(Pattern pattern, BiFunction<Throwable, ContextSpan, WErrorDefinition> analyzer) {
        this.pattern = pattern;
        this.analyzer = analyzer;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public BiFunction<Throwable, ContextSpan, WErrorDefinition> getAnalyzer() {
        return analyzer;
    }
}
