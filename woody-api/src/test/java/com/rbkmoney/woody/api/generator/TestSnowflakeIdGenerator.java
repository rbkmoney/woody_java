package com.rbkmoney.woody.api.generator;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by tolkonepiu on 09.06.16.
 */
public class TestSnowflakeIdGenerator {
    @Test
    public void testSequenceIds() {
        int nodeId = 123;
        String suffix = "test";
        IdGenerator idGenerator = new SnowflakeIdGenerator(suffix, nodeId);
        String id = idGenerator.generateId();
        assertEquals(suffix, id.substring(id.length()-suffix.length()));
        assertEquals(0, Long.parseLong(id.substring(0, id.length() - suffix.length())) & 0b1111111111);
        id = idGenerator.generateId();
        assertEquals(1, Long.parseLong(id.substring(0, id.length() - suffix.length())) & 0b1111111111);
    }
}
