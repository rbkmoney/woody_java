package com.rbkmoney.woody.thrift.impl.http.event;

import com.rbkmoney.woody.api.event.ServiceEvent;
import com.rbkmoney.woody.api.event.ServiceEventListener;

/**
 * Created by vpankrashkin on 12.05.16.
 */
public class ServiceEventListenerImpl implements ServiceEventListener {
    private volatile ServiceActionListener eventActionListener;

    public ServiceEventListenerImpl() {
    }

    public ServiceEventListenerImpl(ServiceActionListener eventActionListener) {
        this.eventActionListener = eventActionListener;
    }

    public void setEventActionListener(ServiceActionListener eventActionListener) {
        this.eventActionListener = eventActionListener;
    }

    @Override
    public void notifyEvent(ServiceEvent event) {
        switch (event.getEventType()) {
            case CALL_HANDLER:
                if (eventActionListener != null)
                    eventActionListener.callHandler(event);
                break;
            case HANDLER_RESULT:
                if (eventActionListener != null)
                    eventActionListener.handlerResult(event);
                break;
            case SERVICE_RECEIVE:
                if (eventActionListener != null)
                    eventActionListener.serviceReceive(event);
                break;
            case SERVICE_RESULT:
                if (eventActionListener != null)
                    eventActionListener.serviceResult(event);
                break;
            case ERROR:
                if (eventActionListener != null)
                    eventActionListener.error(event);
                break;
            default:
                if (eventActionListener != null)
                    eventActionListener.unddefined(event);
                break;

        }
    }
}
