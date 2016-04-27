package com.rbkmoney.woody.api.trace;

/**
 * Created by vpankrashkin on 21.04.16.
 */
public interface Endpoint<S, T> {
    S getSource();

    T getTarget();
}
