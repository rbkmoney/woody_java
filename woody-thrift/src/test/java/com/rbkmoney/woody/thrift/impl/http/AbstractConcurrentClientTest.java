package com.rbkmoney.woody.thrift.impl.http;

import com.rbkmoney.woody.api.event.ClientEventListener;
import com.rbkmoney.woody.api.event.ServiceEventListener;
import com.rbkmoney.woody.api.generator.IdGenerator;
import com.rbkmoney.woody.rpc.Owner;
import com.rbkmoney.woody.rpc.OwnerServiceSrv;
import com.rbkmoney.woody.thrift.impl.http.event.ClientEventListenerImpl;
import com.rbkmoney.woody.thrift.impl.http.event.ClientEventLogListener;
import com.rbkmoney.woody.thrift.impl.http.event.ServiceEventListenerImpl;
import com.rbkmoney.woody.thrift.impl.http.event.ServiceEventLogListener;
import com.rbkmoney.woody.api.generator.TimestampIdGenerator;
import org.apache.thrift.TException;
import org.junit.Assert;
import org.junit.Test;

import javax.servlet.Servlet;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by vpankrashkin on 10.06.16.
 */
public abstract class AbstractConcurrentClientTest extends AbstractTest {
    protected long runTime = 5000;
    protected int nThreads = 8;
    final AtomicInteger clientCalls = new AtomicInteger(0);
    final AtomicInteger serverAccepts = new AtomicInteger(0);

    protected ServiceEventListener serviceEventStub = new ServiceEventListenerImpl();
    protected ServiceEventListener serviceEventLogger = new ServiceEventLogListener();
    protected ClientEventListener clientEventStub = new ClientEventListenerImpl();
    protected ClientEventListener clientEventLogger = new ClientEventLogListener();

    @Test
    public void testPool() throws InterruptedException {
        Servlet servlet = createThriftRPCService(OwnerServiceSrv.Iface.class, new OwnerServiceStub() {
            @Override
            public Owner getOwner(int id) throws TException {
                serverAccepts.incrementAndGet();
                return super.getOwner(id);
            }

            @Override
            public void setOwner(Owner owner) throws TException {
                serverAccepts.incrementAndGet();
                super.setOwner(owner);
            }
        }, serviceEventLogger);
        addServlet(servlet, "/load");
        OwnerServiceSrv.Iface client = createThriftRPCClient(OwnerServiceSrv.Iface.class, new TimestampIdGenerator(), clientEventLogger, getUrlString()+"/load");

        ExecutorService executor = Executors.newFixedThreadPool(nThreads);

        Collection<Callable> callableCollection = Collections.nCopies(nThreads, () -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    client.getOwner(0);
                    clientCalls.incrementAndGet();
                    client.setOwner(new Owner(0, ""));
                    clientCalls.incrementAndGet();
                    //Thread.sleep(100);

                } catch (Exception e) {
                    if (!(e instanceof InterruptedException))
                        e.printStackTrace();
                } finally {

                }
            }
            return null;

        });

        callableCollection.stream().forEach((callable -> executor.submit(callable)));


        Thread watcher = new Thread(() -> {
            int lastCVal = 0;
            int lastSVal = 0;
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(1000);
                    int currCVal = clientCalls.get();
                    int currSVal = serverAccepts.get();
                    System.out.println("C Op/sec:"+ (currCVal - lastCVal));
                    System.out.println("S Op/sec:"+ (currSVal - lastSVal));
                    lastCVal = currCVal;
                    lastSVal = currSVal;
                } catch (InterruptedException e) {
                    return;
                }

            }
        });
        watcher.start();

        Thread.sleep(runTime);

        executor.shutdownNow();
        executor.awaitTermination(1, TimeUnit.SECONDS);
        watcher.interrupt();

        Assert.assertEquals(clientCalls.get(), serverAccepts.get());

    }

    @Override
    protected <T> Servlet createThriftRPCService(Class<T> iface, T handler, ServiceEventListener eventListener) {
        return super.createThriftRPCService(iface, handler, serviceEventStub);
    }

    abstract protected <T> T createThriftRPCClient(Class<T> iface, IdGenerator idGenerator, ClientEventListener eventListener, String url);
}
