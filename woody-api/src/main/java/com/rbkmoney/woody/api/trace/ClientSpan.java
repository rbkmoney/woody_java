package com.rbkmoney.woody.api.trace;

/**
 * Created by vpankrashkin on 21.04.16.
 */
public class ClientSpan extends ContextSpan {

    public ClientSpan() {
    }

    protected ClientSpan(ClientSpan clientSpan) {
        super(clientSpan);
    }

    public ClientSpan cloneObject() {
        return new ClientSpan(this);
    }

}
