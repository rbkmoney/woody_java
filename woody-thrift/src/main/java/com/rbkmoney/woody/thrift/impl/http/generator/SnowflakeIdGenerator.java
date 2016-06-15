package com.rbkmoney.woody.thrift.impl.http.generator;

import com.rbkmoney.woody.api.generator.IdGenerator;
import com.rbkmoney.woody.thrift.impl.http.util.Base62Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.IllegalFormatException;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by tolkonepiu on 09.06.16.
 */
public class SnowflakeIdGenerator implements IdGenerator {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final long sequenceBits = 12L;
    private final long nodeIdBits = 10L;
    private final long maxNodeId = -1L ^ (-1L << nodeIdBits);

    private final long nodeIdShift = sequenceBits;
    private final long timestampLeftShift = sequenceBits + nodeIdBits;

    private final long twepoch = 1288834974657L;
    private final long nodeId;
    private final long sequenceMask = -1L ^ (-1L << sequenceBits);
    private final int maxSuffixLength = 21;

    public static final String DEFAULT_SUFFIX = "root";

    private final AtomicInteger sequence = new AtomicInteger();

    private final String suffix;

    public SnowflakeIdGenerator() {
        this(DEFAULT_SUFFIX);
    }

    public SnowflakeIdGenerator(String suffix) {
        checkSuffix(suffix);

        this.suffix = suffix;
        this.nodeId = generateNodeId();
    }

    public SnowflakeIdGenerator(String suffix, long nodeId) {
        checkSuffix(suffix);

        this.suffix = suffix;
        this.nodeId = nodeId;
    }

    private void checkSuffix(String suffix) {
        if (suffix == null || suffix.isEmpty()) {
            throw new RuntimeException("Suffix value must not be empty");
        }

        if (suffix.getBytes().length > maxSuffixLength) {
            throw new RuntimeException("Suffix value to long");
        }

        if (!suffix.matches("[a-zA-Z0-9.,_-]*")) {
            throw new RuntimeException("Unrecognized symbols in suffix value");
        }
    }

    @Override
    public String generateId(long timestamp) {
        return new StringBuilder().append(nextId(timestamp)).append(':').append(suffix).toString();
    }

    @Override
    public String generateId(long timestamp, int counter) {
        return new StringBuilder().append(nextId(timestamp)).append(':').append(counter).toString();
    }

    private String nextId(long timestamp) {
        long id = ((timestamp - twepoch) << timestampLeftShift) |
                ((nodeId & maxNodeId) << nodeIdShift) |
                (sequence.getAndIncrement() & sequenceMask);
        return Base62Utils.base62Encode(id);
    }

    public String getSuffix() {
        return suffix;
    }

    public long getNodeId() {
        return nodeId;
    }


    protected long generateNodeId() {
        long id = new Random(maxNodeId).nextInt();

        try {
            InetAddress ip = InetAddress.getLocalHost();
            NetworkInterface network = NetworkInterface.getByInetAddress(ip);

            if (network != null) {
                byte[] mac = network.getHardwareAddress();
                id = ((0x000000FF & mac[mac.length - 1]) | (0x0000FF00 & ((mac[mac.length - 2]) << 8))) >> 6;
            }

        } catch (SocketException | UnknownHostException ex) {
            log.debug("Failed generate node id from mac address", ex);
        }
        return id;
    }
}
