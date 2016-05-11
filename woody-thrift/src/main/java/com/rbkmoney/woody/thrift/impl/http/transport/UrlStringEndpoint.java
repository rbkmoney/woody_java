package com.rbkmoney.woody.thrift.impl.http.transport;

import com.rbkmoney.woody.api.trace.Endpoint;

/**
 * Created by vpankrashkin on 06.05.16.
 */
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
}
