package com.rbkmoney.woody.thrift.impl.http.event;

import com.rbkmoney.woody.api.event.ServiceEvent;

/**
 * Created by vpankrashkin on 12.05.16.
 */
public interface ServiceActionListener {
    ServiceEvent callHandler(ServiceEvent event);

    ServiceEvent handlerResult(ServiceEvent event);

    ServiceEvent serviceReceive(ServiceEvent event);

    ServiceEvent serviceResult(ServiceEvent event);

    ServiceEvent error(ServiceEvent event);

    ServiceEvent unddefined(ServiceEvent event);
}
