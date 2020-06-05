package com.rbkmoney.woody.thrift.impl.http.transport;

import com.rbkmoney.woody.api.trace.Endpoint;

public class UrlStringEndpoint implements Endpoint<String> {
    private String url;

    public UrlStringEndpoint(String url) {
        this.url = url;
    }

    @Override
    public String getStringValue() {
        return url;
    }

    @Override
    public String getValue() {
        return url;
    }

    @Override
    public String toString() {
        return "UrlStringEndpoint{" +
                "url='" + url + '\'' +
                '}';
    }
}
