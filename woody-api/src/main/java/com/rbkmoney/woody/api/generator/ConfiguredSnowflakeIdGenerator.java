package com.rbkmoney.woody.api.generator;

/**
 * Created by vpankrashkin on 09.12.16.
 */
public class ConfiguredSnowflakeIdGenerator extends SnowflakeIdGenerator {
    public static final String NODE_ID_ENV_PARAM = "woody.node_id";

    public ConfiguredSnowflakeIdGenerator() {
        this(DEFAULT_SUFFIX);
    }

    public ConfiguredSnowflakeIdGenerator(String suffix) {
        super(suffix, getNodeIdParam());
    }

    private static long getNodeIdParam() {
        return Long.parseLong(System.getProperty(NODE_ID_ENV_PARAM, "-1"));
    }
}
