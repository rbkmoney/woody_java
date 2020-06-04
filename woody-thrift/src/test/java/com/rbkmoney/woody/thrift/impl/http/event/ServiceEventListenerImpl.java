package com.rbkmoney.woody.thrift.impl.http.event;

import com.rbkmoney.woody.api.event.ServiceEventListener;

public class ServiceEventListenerImpl implements ServiceEventListener<THServiceEvent> {
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
    public void notifyEvent(THServiceEvent event) {
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
                    eventActionListener.undefined(event);
                break;

        }
    }
}
