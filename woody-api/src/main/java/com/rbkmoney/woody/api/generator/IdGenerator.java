package com.rbkmoney.woody.api.generator;

public interface IdGenerator {

    String generateId();

    String generateId(String prefix);

    String generateId(String prefix, int counter);
}
