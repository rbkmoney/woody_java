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
    private final CommonInterceptor interceptor;

    /**
     * Encloses the specified protocol.
     *
     * @param protocol All operations will be forward to this protocol.  Must be non-null.
     */
    public THSProtocolWrapper(TProtocol protocol, CommonInterceptor interceptor) {
        super(protocol);
        this.interceptor = interceptor;
    }

    @Override
    public TMessage readMessageBegin() throws TException {
        TMessage tMessage = super.readMessageBegin();
        //todo process state
        interceptor.interceptRequest(TraceContext.getCurrentTraceData(), tMessage);
        return tMessage;
    }

    @Override
    public void writeMessageBegin(TMessage tMessage) throws TException {
        //todo process state
        interceptor.interceptResponse(TraceContext.getCurrentTraceData(), tMessage);
        super.writeMessageBegin(tMessage);
    }


}
