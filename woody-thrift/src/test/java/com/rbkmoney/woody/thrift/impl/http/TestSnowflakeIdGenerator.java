package com.rbkmoney.woody.thrift.impl.http;

import com.rbkmoney.woody.api.generator.IdGenerator;
import com.rbkmoney.woody.thrift.impl.http.generator.SnowflakeIdGenerator;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;

/**
 * Created by tolkonepiu on 09.06.16.
 */
public class TestSnowflakeIdGenerator {

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Test
    public void checkIds() {
        expectedEx.expect(IllegalStateException.class);
        expectedEx.expectMessage("clock moved backwards. refusing to generate id for 30000000 milliseconds.");

        long timestamp1 = 1465564339290L;
        long timestamp2 = 1465564339295L;
        long timestamp3 = 1465534339295L;

        IdGenerator idGenerator = new SnowflakeIdGenerator("test", 123);
        assertEquals("a4979062647b000:test", idGenerator.generateId(timestamp1));
        assertEquals("a4979062787b000:test", idGenerator.generateId(timestamp2));
        //should throw runtime exception
        idGenerator.generateId(timestamp3);
    }

}
