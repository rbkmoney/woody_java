package com.rbkmoney.woody.api.generator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

/**
 * Created by tolkonepiu on 09.06.16.
 */
public class SnowflakeIdGenerator implements IdGenerator {

    public static final String DEFAULT_SUFFIX = "";

    public static final long EPOCH_OFFSET = LocalDateTime.of(2012, 1, 1, 0, 0).toInstant(ZoneOffset.UTC).toEpochMilli();

    private static final int MAX_ID_LENGTH = 32;

    private static final int MAX_GENERATED_ID_LENGTH = 19;

    private static final Pattern ID_PATTERN = Pattern.compile("[a-zA-Z0-9.,_-]*");
    private static final int SEQUENCE_BITS = 12;

    private static final int NODE_ID_BITS = 10;
    private static final long SEQUENCE_MASK = -1L ^ (-1L << SEQUENCE_BITS);

    private static final int NODE_ID_MASK = -1 ^ (-1 << NODE_ID_BITS);
    private static final int NODE_ID_SHIFT = SEQUENCE_BITS;

    private static final int TIMESTAMP_SHIFT = SEQUENCE_BITS + NODE_ID_BITS;

    private static final int MAX_ADDITIVE_LENGTH = MAX_ID_LENGTH - MAX_GENERATED_ID_LENGTH;

    private static final Logger log = LoggerFactory.getLogger(SnowflakeIdGenerator.class);

    private final long nodeId;

    private final AtomicInteger sequence = new AtomicInteger();

    private final String suffix;

    public SnowflakeIdGenerator() {
        this(DEFAULT_SUFFIX);
    }

    public SnowflakeIdGenerator(long nodeId) {
        this(DEFAULT_SUFFIX, nodeId);
    }

    public SnowflakeIdGenerator(String suffix) {
        checkSuffix(suffix);
        this.suffix = suffix;
        this.nodeId = narrowNodeId(generateNodeId(), NODE_ID_MASK);
    }

    public SnowflakeIdGenerator(String suffix, long nodeId) {
        checkSuffix(suffix);

        this.suffix = suffix;
        this.nodeId = narrowNodeId(nodeId >= 0 ? nodeId : generateNodeId(), NODE_ID_MASK);
    }

    @Override
    public String generateId() {
        return new StringBuilder().append(nextId(generateTimestamp())).append(suffix).toString();
    }

    @Override
    public String generateId(String prefix) {
        checkPrefix(prefix);
        return new StringBuilder().append(prefix).append(nextId(generateTimestamp())).append(suffix).toString();
    }

    @Override
    public String generateId(String prefix, int counter) {
        return generateId(prefix);
    }

    public String getSuffix() {
        return suffix;
    }

    public long getNodeId() {
        return nodeId;
    }

    protected String encodeId(long id) {
        return Long.toString(id);
    }

    protected long generateTimestamp() {
        return System.currentTimeMillis();
    }

    private void checkSuffix(String suffix) {
        checkAdditive(suffix, MAX_ADDITIVE_LENGTH);
    }

    private void checkPrefix(String prefix) {
        checkAdditive(prefix, MAX_ADDITIVE_LENGTH - suffix.length());
    }

    private void checkAdditive(String part, int maxLength) {
        if (part == null) {
            throw new IllegalArgumentException("Value must not be null");
        }

        if (part.length() > maxLength) {
            throw new IllegalArgumentException("Value to long");
        }

        if (!ID_PATTERN.matcher(part).matches()) {
            throw new IllegalArgumentException("Unrecognized symbols in value");
        }
    }

    private String nextId(long timestamp) {
        long seq = sequence.getAndAccumulate(1, (prev, x) -> prev+x >= SEQUENCE_MASK ? 0 : prev+x);
        long id = ((timestamp - EPOCH_OFFSET) << TIMESTAMP_SHIFT) |
                (nodeId << NODE_ID_SHIFT) |
                (seq & SEQUENCE_MASK);
        return encodeId(id);
    }

    /**
     * @return id not more than 0xFFFF
     * */
    private long generateNodeId() {
        try {
            InetAddress ip = InetAddress.getLocalHost();
            NetworkInterface network = NetworkInterface.getByInetAddress(ip);

            if (network != null) {
                int hash = Arrays.hashCode(network.getHardwareAddress());
                return (hash ^ (hash >> 16)) & 0xFFFF;
            }
        } catch (SocketException | UnknownHostException ex) {
            log.debug("Failed generate node id from mac address", ex);
        }
        return 0;
    }

    private long narrowNodeId(long nodeId, int maxNodeId) {
        return nodeId < maxNodeId ? nodeId : maxNodeId;
    }
}
