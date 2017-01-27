package com.rbkmoney.woody.api.generator;

/**
 * Created by vpankrashkin on 22.04.16.
 */
public interface IdGenerator {

    String generateId();

    String generateId(String prefix);

    String generateId(String prefix, int counter);
}
