package com.rbkmoney.woody.thrift.impl.http.event;

import com.rbkmoney.woody.api.event.ClientEventListener;

public class ClientEventListenerImpl implements ClientEventListener<THClientEvent> {
    private ClientActionListener eventActionListener;

    public ClientEventListenerImpl() {
    }

    public ClientEventListenerImpl(ClientActionListener eventActionListener) {
        this.eventActionListener = eventActionListener;
    }

    public void setEventActionListener(ClientActionListener eventActionListener) {
        this.eventActionListener = eventActionListener;
    }

    @Override
    public void notifyEvent(THClientEvent event) {
        switch (event.getEventType()) {
            case CALL_SERVICE:
                if (eventActionListener != null)
                    eventActionListener.callService(event);
                break;
            case CLIENT_SEND:
                if (eventActionListener != null)
                    eventActionListener.clientSend(event);
                break;
            case CLIENT_RECEIVE:
                if (eventActionListener != null)
                    eventActionListener.clientReceive(event);
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
