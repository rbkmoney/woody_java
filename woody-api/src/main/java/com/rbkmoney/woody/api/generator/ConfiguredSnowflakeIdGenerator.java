package com.rbkmoney.woody.api.generator;

public class ConfiguredSnowflakeIdGenerator extends SnowflakeIdGenerator {
    public static final String NODE_ID_ENV_PARAM = "woody.node_id";

    public ConfiguredSnowflakeIdGenerator() {
        super(getNodeIdParam());
    }

    public ConfiguredSnowflakeIdGenerator(String suffix) {
        super(suffix, getNodeIdParam());
    }

    private static long getNodeIdParam() {
        return Long.parseLong(System.getProperty(NODE_ID_ENV_PARAM, "-1"));
    }
}
