package com.rbkmoney.woody.rpc;

import org.apache.thrift.TException;

/**
 * Created by vpankrashkin on 19.04.16.
 */
public class OwnerServiceImpl implements OwnerService.Iface {
    @Override
    public Owner getOwner(int id) throws TException {
        return new Owner(1, "name");
    }

    @Override
    public Owner getErrOwner(int id) throws test_error, TException {
        return null;
    }

    @Override
    public void setOwner(Owner owner) throws TException {

    }

    @Override
    public void setOwnerOneway(Owner owner) throws TException {

    }

    @Override
    public Owner setErrOwner(Owner owner, int id) throws TException {
        return null;
    }
}
