package com.rbkmoney.woody.thrift.impl.http;

import com.rbkmoney.woody.rpc.OwnerServiceSrv;
import org.apache.thrift.TException;
import org.junit.Ignore;
import org.junit.Test;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import static org.junit.Assert.*;

/**
 * Created by vpankrashkin on 05.05.16.
 */
@Ignore
public class TestThriftRPCClientMethods {


    @Test
    public void testClientMethods() throws TException, URISyntaxException {
        String rpcServletUrl = "http://localhost:8080";
        OwnerServiceSrv.Iface tRPCClient1 = createThriftRPCClient(rpcServletUrl);
        OwnerServiceSrv.Iface tRPCClient2 = createThriftRPCClient(rpcServletUrl);
        assertEquals(tRPCClient1.hashCode(), tRPCClient1.hashCode());
        assertEquals(tRPCClient2.hashCode(), tRPCClient2.hashCode());

        assertTrue(tRPCClient1.equals(tRPCClient1));
        assertTrue(tRPCClient2.equals(tRPCClient2));
        assertFalse(tRPCClient1.equals(tRPCClient2));
        assertFalse(tRPCClient2.equals(tRPCClient1));

        assertTrue(tRPCClient1.toString().startsWith("com.rbkmoney.woody.rpc.OwnerServiceSrv$Iface@"));
        assertTrue(tRPCClient2.toString().startsWith("com.rbkmoney.woody.rpc.OwnerServiceSrv$Iface@"));

        Map map = new HashMap<>();
        map.put(tRPCClient1, tRPCClient1);
        assertTrue(map.containsKey(tRPCClient1));
        assertSame(map.get(tRPCClient1), tRPCClient1);
        map.put(tRPCClient1, tRPCClient1);
        assertEquals(1, map.size());

        map.put(tRPCClient2, tRPCClient2);
        assertTrue(map.containsKey(tRPCClient2));
        assertSame(map.get(tRPCClient2), tRPCClient2);
        map.put(tRPCClient2, tRPCClient2);
        assertEquals(2, map.size());
    }

    private OwnerServiceSrv.Iface createThriftRPCClient(String url) throws URISyntaxException {
        THClientBuilder clientBuilder = new THClientBuilder();
        clientBuilder.withAddress(new URI(url));
        return clientBuilder.build(OwnerServiceSrv.Iface.class);
    }

}
