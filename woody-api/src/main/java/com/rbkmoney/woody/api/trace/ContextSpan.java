package com.rbkmoney.woody.api.trace;

/**
 * Created by vpankrashkin on 22.04.16.
 */
public class ContextSpan {
    protected final Span span = new Span();
    protected final Metadata metadata = new Metadata();

    public Span getSpan() {
        return span;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public boolean isFilled() {
        return span.isFilled();
    }

    public boolean isStarted() {
        return span.isStarted();
    }

    public void reset() {
        span.reset();
        metadata.reset();
    }
}
