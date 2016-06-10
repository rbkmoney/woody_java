package com.rbkmoney.woody.thrift.impl.http.generator;

import com.rbkmoney.woody.api.generator.IdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Random;

/**
 * Created by tolkonepiu on 09.06.16.
 */
public class SnowflakeIdGenerator implements IdGenerator {

    Logger log = LoggerFactory.getLogger(this.getClass());

    private final long sequenceBits = 12;
    private final long nodeIdBits = 10L;
    private final long maxNodeId = -1L ^ (-1L << nodeIdBits);

    private final long nodeIdShift = sequenceBits;
    private final long timestampLeftShift = sequenceBits + nodeIdBits;

    private final long twepoch = 1288834974657L;
    private final long nodeId;
    private final long sequenceMask = -1L ^ (-1L << sequenceBits);

    private volatile long lastTimestamp = -1L;
    private volatile long sequence = 0L;

    private final String suffix;

    public SnowflakeIdGenerator() {
        this("root");
    }

    public SnowflakeIdGenerator(String suffix) {
        this.suffix = suffix;
        this.nodeId = generateNodeId();
    }

    public SnowflakeIdGenerator(String suffix, long nodeId) {
        this.suffix = suffix;
        this.nodeId = nodeId;
    }

    @Override
    public String generateId(long timestamp) {
        return new StringBuilder().append(nextId(timestamp)).append(':').append(suffix).toString();
    }

    @Override
    public String generateId(long timestamp, int counter) {
        return new StringBuilder().append(nextId(timestamp)).append(':').append(counter).toString();
    }

    public synchronized String nextId(long timestamp) {
        if (timestamp < lastTimestamp) {
            throw new RuntimeException(String.format("clock moved backwards. refusing to generate id for %d milliseconds.", lastTimestamp - timestamp));
        }
        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & sequenceMask;
            if (sequence == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0;
        }
        lastTimestamp = timestamp;
        Long id = ((timestamp - twepoch) << timestampLeftShift) |
                (nodeId << nodeIdShift) |
                sequence;
        return Long.toHexString(id);
    }

    protected String getSuffix() {
        return suffix;
    }

    protected long getNodeId() {
        return nodeId;
    }

    protected long tilNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
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
