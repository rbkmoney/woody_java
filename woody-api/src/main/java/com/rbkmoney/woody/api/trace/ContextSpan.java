package com.rbkmoney.woody.api.trace;

public class ContextSpan {
    protected final Span span;
    protected final Metadata metadata;
    protected final Metadata customMetadata;

    public ContextSpan() {
        span = new Span();
        metadata = new Metadata();
        customMetadata = new Metadata(false);
    }

    protected ContextSpan(ContextSpan oldSpan) {
        this.span = oldSpan.span.cloneObject();
        this.metadata = oldSpan.metadata.cloneObject();
        this.customMetadata = oldSpan.customMetadata.cloneObject();
    }

    protected ContextSpan(ContextSpan oldSpan, Metadata customMetadata) {
        this.span = oldSpan.span.cloneObject();
        this.metadata = oldSpan.metadata.cloneObject();
        this.customMetadata = customMetadata.cloneObject();
    }

    public Span getSpan() {
        return span;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public Metadata getCustomMetadata() {
        return customMetadata;
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
        customMetadata.reset();
    }

    public ContextSpan cloneObject() {
        return new ContextSpan(this);
    }
}
