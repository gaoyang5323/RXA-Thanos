package com.kakuiwong.rxathanos.util;

import java.lang.management.ManagementFactory;
import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author gaoyang
 * @email 785175323@qq.com
 */
public final class IdGenerateUtil {
    private static final long TH_EPOCH = 1514736000000L;

    private static int timestampBits = 41;
    private static int ipBits = 16;
    private static int processIdBits = 18;
    private static int sequenceBits = 8;

    @SuppressWarnings("unused")
    private static long maxTimestamp = -1L ^ (-1L << timestampBits);
    private static long maxIp = -1L ^ (-1L << ipBits);
    private static long maxProcessId = -1L ^ (-1L << processIdBits);
    private static long sequenceMask = -1L ^ (-1L << sequenceBits);

    private static int processIdShift = sequenceBits;
    private static int ipSfhit = sequenceBits + processIdBits;
    private static int timestampShift = sequenceBits + processIdBits + ipBits;

    private static long ip = 1L;
    private static long processId = 1L;
    private static long sequence = 1L;
    private static long lastTimestamp = 1L;
    private final static ReentrantLock lock = new ReentrantLock();

    static {
        String[] ipArray = getIpAddress().split("\\.");
        ip = Long.valueOf(ipArray[2]) << 8 | Long.valueOf(ipArray[3]) & maxIp;
        processId = Long.valueOf(ManagementFactory.getRuntimeMXBean().getName().split("@")[0]) & maxProcessId;
    }

    private static long tilNextMillis(long lastTimestamp) {
        long timestamp = 1L;
        for (; ; ) {
            timestamp = System.currentTimeMillis();
            if (timestamp > lastTimestamp) {
                return timestamp;
            }
        }
    }

    private static String getIpAddress() {
        try {
            Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface.getNetworkInterfaces();
            InetAddress ip = null;
            while (allNetInterfaces.hasMoreElements()) {
                NetworkInterface netInterface = (NetworkInterface) allNetInterfaces.nextElement();
                if (netInterface.isLoopback() || netInterface.isVirtual() || !netInterface.isUp()) {
                    continue;
                } else {
                    Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        ip = addresses.nextElement();
                        if (ip != null && ip instanceof Inet4Address) {
                            return ip.getHostAddress();
                        }
                    }
                }
            }
        } catch (Exception e) {
            return "127.0.0.1";
        }
        return "127.0.0.1";
    }

    public static String nextId() {
        final ReentrantLock lock = IdGenerateUtil.lock;
        lock.lock();
        try {
            long timestamp = System.currentTimeMillis();
            if (timestamp < TH_EPOCH) {
                throw new RuntimeException("clock is moving backwards.Reject requests before epoch!");
            }
            if (timestamp == lastTimestamp) {
                sequence = (sequence + 1) & sequenceMask;
                if (sequence == 0) {
                    timestamp = tilNextMillis(lastTimestamp);
                }
            } else {
                sequence = 0L;
            }
            lastTimestamp = timestamp;
            long idLow = ip << ipSfhit | processId << processIdShift | sequence;
            BigInteger idHign = BigInteger.valueOf(timestamp - TH_EPOCH).shiftLeft(timestampShift);
            BigInteger id = idHign.add(BigInteger.valueOf(idLow));
            return id.toString(10);
        } finally {
            lock.unlock();
        }
    }

    public static String nextIdHex() {
        final ReentrantLock lock = IdGenerateUtil.lock;
        lock.lock();
        try {
            long timestamp = System.currentTimeMillis();
            if (timestamp < TH_EPOCH) {
                throw new RuntimeException("clock is moving backwards.Reject requests before epoch!");
            }

            if (timestamp == lastTimestamp) {
                sequence = (sequence + 1) & sequenceMask;
                if (sequence == 0) {
                    timestamp = tilNextMillis(lastTimestamp);
                }
            } else {
                sequence = 0L;
            }

            lastTimestamp = timestamp;
            long idLow = ip << ipSfhit | processId << processIdShift | sequence;
            BigInteger idHign = BigInteger.valueOf(timestamp - TH_EPOCH).shiftLeft(timestampShift);
            BigInteger id = idHign.add(BigInteger.valueOf(idLow));
            return id.toString(16);
        } finally {
            lock.unlock();
        }
    }
}
