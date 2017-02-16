package com.rbkmoney.woody.thrift.impl.http;

import com.rbkmoney.woody.api.interceptor.CommonInterceptor;
import com.rbkmoney.woody.api.trace.context.TraceContext;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TMessage;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolDecorator;

/**
 * Created by vpankrashkin on 10.05.16.
 */
public class THSProtocolWrapper extends TProtocolDecorator {
    private final boolean isCLient;
    private final CommonInterceptor interceptor;
    private final TProtocol protocol;

    /**
     * Encloses the specified protocol.
     *
     * @param protocol All operations will be forward to this protocol.  Must be non-null.
     */
    public THSProtocolWrapper(TProtocol protocol, CommonInterceptor interceptor, boolean isCLient) {
        super(protocol);
        this.protocol = protocol;
        this.interceptor = interceptor;
        this.isCLient = isCLient;
    }

    public TProtocol getProtocol() {
        return protocol;
    }

    @Override
    public TMessage readMessageBegin() throws TException {
        TMessage tMessage = super.readMessageBegin();
        //todo process state
        if (isCLient) {
            interceptor.interceptResponse(TraceContext.getCurrentTraceData(), tMessage);
        } else {
            interceptor.interceptRequest(TraceContext.getCurrentTraceData(), tMessage);
        }
        return tMessage;
    }

    @Override
    public void writeMessageBegin(TMessage tMessage) throws TException {
        //todo process state
        if (isCLient) {
            interceptor.interceptRequest(TraceContext.getCurrentTraceData(), tMessage);
        } else {
            interceptor.interceptResponse(TraceContext.getCurrentTraceData(), tMessage);
        }
        super.writeMessageBegin(tMessage);
    }


}
