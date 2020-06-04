package com.rbkmoney.woody.thrift.impl.http.event;

import com.rbkmoney.woody.api.event.ClientEvent;

public interface ClientActionListener {
    ClientEvent callService(ClientEvent event);

    ClientEvent clientSend(ClientEvent event);

    ClientEvent clientReceive(ClientEvent event);

    ClientEvent serviceResult(ClientEvent event);

    ClientEvent error(ClientEvent event);

    ClientEvent undefined(ClientEvent event);
}
