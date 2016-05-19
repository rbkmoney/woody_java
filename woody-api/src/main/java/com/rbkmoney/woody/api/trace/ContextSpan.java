package com.rbkmoney.woody.api.trace;

/**
 * Created by vpankrashkin on 22.04.16.
 */
public class ContextSpan {
    protected final Span span;
    protected final Metadata metadata;

    public ContextSpan() {
        span = new Span();
        metadata = new Metadata();
    }

    protected ContextSpan(ContextSpan oldSpan) {
        this.span = oldSpan.span.cloneObject();
        this.metadata = oldSpan.metadata.cloneObject();
    }

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

    public ContextSpan cloneObject() {
        return new ContextSpan(this);
    }
}
