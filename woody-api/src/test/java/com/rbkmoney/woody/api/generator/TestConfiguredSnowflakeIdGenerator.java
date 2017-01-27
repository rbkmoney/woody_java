package com.rbkmoney.woody.api.generator;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Created by vpankrashkin on 09.12.16.
 */
public class TestConfiguredSnowflakeIdGenerator {
    @Test
    public void testCheckNodeIdsWithoutEnv() {

        String prefix = "prefix";
        String suffix = "test";
        IdGenerator idGenerator = new ConfiguredSnowflakeIdGenerator(suffix);
        String strId = idGenerator.generateId(prefix);
        long id = Long.parseLong(strId.substring(strId.indexOf(prefix)+prefix.length(), strId.indexOf(suffix)));
        assertEquals(0, id & (-1 ^ (-1 << 12)));
    }

    @Test
    public void testCheckNodeIdsWithEnv() {
        int nodeId = 0b1111111111;
        String suffix = "test";
        System.setProperty(ConfiguredSnowflakeIdGenerator.NODE_ID_ENV_PARAM, nodeId+"");
        IdGenerator idGenerator = new ConfiguredSnowflakeIdGenerator(suffix);
        String strId = idGenerator.generateId();
        long id = Long.parseLong(strId.substring(0, strId.indexOf(suffix)));
        assertEquals(nodeId, (id >> 12) & ((-1 ^ (-1 << 22))) >> 12);
        assertEquals(0, id & (-1 ^ (-1 << 12)));
    }
}
