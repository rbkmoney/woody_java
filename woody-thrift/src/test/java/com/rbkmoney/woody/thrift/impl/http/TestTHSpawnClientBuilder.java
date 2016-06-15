package com.rbkmoney.woody.thrift.impl.http;

import com.rbkmoney.woody.api.event.ClientEventListener;
import com.rbkmoney.woody.api.generator.IdGenerator;
import org.apache.http.impl.client.HttpClientBuilder;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by vpankrashkin on 10.06.16.
 */
public class TestTHSpawnClientBuilder extends AbstractConcurrentClientTest {

    protected <T> T createThriftRPCClient(Class<T> iface, IdGenerator idGenerator, ClientEventListener eventListener, String url) {
        try {
            THSpawnClientBuilder clientBuilder = new THSpawnClientBuilder();
            clientBuilder.withAddress(new URI(url));
            clientBuilder.withHttpClient(HttpClientBuilder.create().build());
            clientBuilder.withIdGenerator(idGenerator);
            clientBuilder.withEventListener(eventListener);
            return clientBuilder.build(iface);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
