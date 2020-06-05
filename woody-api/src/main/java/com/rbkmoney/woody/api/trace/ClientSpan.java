package com.rbkmoney.woody.api.trace;

public class ClientSpan extends ContextSpan {

    public ClientSpan() {
    }

    protected ClientSpan(ClientSpan clientSpan) {
        super(clientSpan);
    }

    protected ClientSpan(ContextSpan oldSpan, Metadata customMetadata) {
        super(oldSpan, customMetadata);
    }

    public ClientSpan cloneObject() {
        return new ClientSpan(this);
    }

}
