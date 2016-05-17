package com.rbkmoney.woody.thrift.impl.http;

import com.rbkmoney.woody.rpc.Owner;
import com.rbkmoney.woody.rpc.OwnerService;
import com.rbkmoney.woody.rpc.test_error;
import org.apache.thrift.TException;

/**
 * Created by vpankrashkin on 19.04.16.
 */
public class OwnerServiceStub implements OwnerService.Iface {
    @Override
    public Owner getOwner(int id) throws TException {
        return new Owner(id, "" + id);
    }

    @Override
    public Owner getErrOwner(int id) throws test_error, TException {
        throw new test_error(id);
    }

    @Override
    public void setOwner(Owner owner) throws TException {

    }

    @Override
    public void setOwnerOneway(Owner owner) throws TException {

    }

    @Override
    public Owner setErrOwner(Owner owner, int id) throws TException {
        throw new test_error(id);
    }
}
