package com.rbkmoney.woody.test_woody;

import com.rbkmoney.damsel.payment_processing.EventSinkSrv;
import com.rbkmoney.woody.api.ClientBuilder;
import com.rbkmoney.woody.api.event.ClientEventListener;
import com.rbkmoney.woody.api.event.CompositeClientEventListener;
import com.rbkmoney.woody.thrift.impl.http.THSpawnClientBuilder;
import com.rbkmoney.woody.thrift.impl.http.event.ClientEventLogListener;
import com.rbkmoney.woody.thrift.impl.http.event.HttpClientEventLogListener;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by inalarsanukaev on 23.01.17.
 */
public class EventSink {
    public final static String HG_URI = "http://localhost:8022/v1/processing/eventsink";

    public EventSinkSrv.Iface eventSinkSrv() throws IOException, URISyntaxException {
        return eventSinkSrv(HG_URI);
    }

    public EventSinkSrv.Iface eventSinkSrv(String url) throws IOException, URISyntaxException {
        return clientBuilder()
                .withEventListener(eventListener())
                .withAddress(new URI(url)).build(EventSinkSrv.Iface.class);
    }

    public ClientEventListener eventListener() {
        return new CompositeClientEventListener(
                new ClientEventLogListener(),
                new HttpClientEventLogListener());
    }

    public ClientBuilder clientBuilder() {
        return new THSpawnClientBuilder();
    }
}
