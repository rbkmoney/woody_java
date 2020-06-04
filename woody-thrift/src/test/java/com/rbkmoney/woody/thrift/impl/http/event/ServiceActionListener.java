package com.rbkmoney.woody.thrift.impl.http.event;

import com.rbkmoney.woody.api.event.ServiceEvent;

public interface ServiceActionListener {
    ServiceEvent callHandler(ServiceEvent event);

    ServiceEvent handlerResult(ServiceEvent event);

    ServiceEvent serviceReceive(ServiceEvent event);

    ServiceEvent serviceResult(ServiceEvent event);

    ServiceEvent error(ServiceEvent event);

    ServiceEvent undefined(ServiceEvent event);
}
