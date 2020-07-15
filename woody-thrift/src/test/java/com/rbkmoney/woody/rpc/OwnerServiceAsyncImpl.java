package com.rbkmoney.woody.rpc;

import org.apache.thrift.TException;
import org.apache.thrift.async.AsyncMethodCallback;

public class OwnerServiceAsyncImpl implements OwnerServiceSrv.AsyncIface {
    @Override
    public void getIntValue(AsyncMethodCallback<Integer> resultHandler) throws TException {
        resultHandler.onComplete(0);
    }

    @Override
    public void getOwner(int id, AsyncMethodCallback<Owner> resultHandler) throws TException {
        resultHandler.onComplete(new Owner(1, "name"));
    }

    @Override
    public void getErrOwner(int id, AsyncMethodCallback<Owner> resultHandler) throws TException {
        resultHandler.onError(new test_error());
    }

    @Override
    public void setOwner(Owner owner, AsyncMethodCallback<Void> resultHandler) throws TException {
        resultHandler.onComplete(null);
    }

    @Override
    public void setOwnerOneway(Owner owner, AsyncMethodCallback<Void> resultHandler) throws TException {
        resultHandler.onComplete(null);
    }

    @Override
    public void setErrOwner(Owner owner, int id, AsyncMethodCallback<Owner> resultHandler) throws TException {
        resultHandler.onError(new TException());
    }
}
