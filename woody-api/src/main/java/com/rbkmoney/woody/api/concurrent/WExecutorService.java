package com.rbkmoney.woody.api.concurrent;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

public class WExecutorService implements ExecutorService, Closeable {

    private final ExecutorService wrappedExecutor;

    /**
     * Creates a new instance.
     *
     * @param wrappedExecutor Wrapped ExecutorService to which execution will be delegated.
     */
    public WExecutorService(final ExecutorService wrappedExecutor) {
        this.wrappedExecutor = wrappedExecutor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(final Runnable arg0) {
        final WRunnable wRunnable = WRunnable.create(arg0);
        wrappedExecutor.execute(wRunnable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean awaitTermination(final long timeout, final TimeUnit unit) throws InterruptedException {
        return wrappedExecutor.awaitTermination(timeout, unit);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> List<Future<T>> invokeAll(final Collection<? extends Callable<T>> arg0) throws InterruptedException {

        return wrappedExecutor.invokeAll(buildWCollection(arg0));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> List<Future<T>> invokeAll(final Collection<? extends Callable<T>> arg0, final long arg1, final TimeUnit arg2)
            throws InterruptedException {
        return wrappedExecutor.invokeAll(buildWCollection(arg0), arg1, arg2);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T invokeAny(final Collection<? extends Callable<T>> arg0) throws InterruptedException, ExecutionException {
        return wrappedExecutor.invokeAny(buildWCollection(arg0));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T invokeAny(final Collection<? extends Callable<T>> arg0, final long arg1, final TimeUnit arg2)
            throws InterruptedException, ExecutionException, TimeoutException {
        return wrappedExecutor.invokeAny(buildWCollection(arg0), arg1, arg2);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isShutdown() {
        return wrappedExecutor.isShutdown();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isTerminated() {
        return wrappedExecutor.isTerminated();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void shutdown() {
        wrappedExecutor.shutdown();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Runnable> shutdownNow() {
        return wrappedExecutor.shutdownNow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> Future<T> submit(final Callable<T> arg0) {
        final WCallable<T> wCallable = WCallable.create(arg0);
        return wrappedExecutor.submit(wCallable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Future<?> submit(final Runnable arg0) {
        final WRunnable wRunnable = WRunnable.create(arg0);
        return wrappedExecutor.submit(wRunnable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> Future<T> submit(final Runnable arg0, final T arg1) {
        final WRunnable wRunnable = WRunnable.create(arg0);
        return wrappedExecutor.submit(wRunnable, arg1);
    }

    private <T> Collection<? extends Callable<T>> buildWCollection(
            final Collection<? extends Callable<T>> originalCollection) {
        final Collection<Callable<T>> collection = new ArrayList<Callable<T>>();
        for (final Callable<T> t : originalCollection) {
            collection.add(WCallable.create(t));
        }
        return collection;
    }

    /**
     * Convenience for try-with-resources, or frameworks such as Spring that automatically process this.
     **/
    @Override
    public void close() {
        shutdown();
    }
}
