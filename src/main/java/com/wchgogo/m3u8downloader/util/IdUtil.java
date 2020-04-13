package com.wchgogo.m3u8downloader.util;

/**
 * Author: Wang Chao
 * Date: 2020/3/28 20:08
 * Description: 类似snowflake的id生成器
 */
public class IdUtil {
    private static IdGenerator idGenerator = new IdGenerator(0, 0); // 多机部署时修改此处

    public static long nextId() {
        return idGenerator.nextId();
    }

    public static class IdGenerator {
        private static final long epoch = 1577808000000L;
        private static final long workerIdShift = 12;
        private static final long dataCenterIdShift = 17;
        private static final long timestampLeftShift = 22;
        private static final long maxWorkerId = 31;
        private static final long maxDataCenterId = 31;
        private static final long maxSequence = 4095;

        private long workerId;
        private long dataCenterId;
        private long sequence = 0L;
        private long lastTimestamp = -1L;

        IdGenerator(long dataCenterId, long workerId) {
            if (dataCenterId > maxDataCenterId || dataCenterId < 0) {
                throw new IllegalArgumentException(String.format("datacenter Id can't be greater than %d or less than 0", maxDataCenterId));
            }
            if (workerId > maxWorkerId || workerId < 0) {
                throw new IllegalArgumentException(String.format("worker Id can't be greater than %d or less than 0", maxWorkerId));
            }
            this.dataCenterId = dataCenterId;
            this.workerId = workerId;
        }

        /**
         * 获得下一个ID (该方法是线程安全的)
         *
         * @return SnowflakeId
         */
        synchronized long nextId() {
            long timestamp = timeGen();

            if (timestamp < lastTimestamp) {
                throw new RuntimeException(String.format("Clock moved backwards.  Refusing to generate id for %d milliseconds", lastTimestamp - timestamp));
            }

            if (lastTimestamp == timestamp) {
                if (++sequence > maxSequence) {
                    sequence = 0L;
                    timestamp = tilNextMillis(lastTimestamp);
                }
            } else {
                sequence = 0L;
            }

            lastTimestamp = timestamp;

            return ((timestamp - epoch) << timestampLeftShift)
                    | (dataCenterId << dataCenterIdShift)
                    | (workerId << workerIdShift)
                    | sequence;
        }

        long tilNextMillis(long lastTimestamp) {
            long timestamp = timeGen();
            while (timestamp <= lastTimestamp) {
                timestamp = timeGen();
            }
            return timestamp;
        }

        long timeGen() {
            return System.currentTimeMillis();
        }
    }

}
