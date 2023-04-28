package com.saltynote.service.generator;

import com.saltynote.service.exception.ClockBackwardException;

/**
 * This algorithm is copied from https://github.com/beyondfengyu/SnowFlake
 *
 * twitter的snowflake算法 -- java实现
 *
 * @author beyond
 * @date 2016/11/26
 */
public class SnowflakeIdGenerator implements IdGenerator {

    /**
     * 起始的时间戳: Birthday of Secret 0505/2022 20:16
     */
    private static final long START_STAMP = 1651806960000L;

    /**
     * 每一部分占用的位数
     */
    private static final long SEQUENCE_BIT = 13; // 序列号占用的位数

    private static final long MACHINE_BIT = 4; // 机器标识占用的位数

    private static final long DATACENTER_BIT = 3;// 数据中心占用的位数

    /**
     * 每一部分的最大值
     */
    private static final long MAX_DATACENTER_NUM = ~(-1L << DATACENTER_BIT);

    private static final long MAX_MACHINE_NUM = ~(-1L << MACHINE_BIT);

    private static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BIT);

    /**
     * 每一部分向左的位移
     */
    private static final long MACHINE_LEFT = SEQUENCE_BIT;

    private static final long DATACENTER_LEFT = SEQUENCE_BIT + MACHINE_BIT;

    private static final long TIMESTAMP_LEFT = DATACENTER_LEFT + DATACENTER_BIT;

    private final long datacenterId; // 数据中心

    private final long machineId; // 机器标识

    private long sequence = 0L; // 序列号

    private long lastStamp = -1L; // 上一次时间戳

    public SnowflakeIdGenerator(long datacenterId, long machineId) {
        if (datacenterId > MAX_DATACENTER_NUM || datacenterId < 0) {
            throw new IllegalArgumentException("datacenterId can't be greater than MAX_DATACENTER_NUM or less than 0");
        }
        if (machineId > MAX_MACHINE_NUM || machineId < 0) {
            throw new IllegalArgumentException("machineId can't be greater than MAX_MACHINE_NUM or less than 0");
        }
        this.datacenterId = datacenterId;
        this.machineId = machineId;
    }

    /**
     * 产生下一个ID
     */
    @Override
    public synchronized long nextId() {
        long currStamp = getCurrentTimestamp();
        if (currStamp < lastStamp) {
            throw new ClockBackwardException("Clock moved backwards.  Refusing to generate id");
        }

        if (currStamp == lastStamp) {
            // 相同毫秒内，序列号自增
            sequence = (sequence + 1) & MAX_SEQUENCE;
            // 同一毫秒的序列数已经达到最大
            if (sequence == 0L) {
                currStamp = getNextMill();
            }
        }
        else {
            // 不同毫秒内，序列号置为0
            sequence = 0L;
        }

        lastStamp = currStamp;

        return (currStamp - START_STAMP) << TIMESTAMP_LEFT // 时间戳部分
                | datacenterId << DATACENTER_LEFT // 数据中心部分
                | machineId << MACHINE_LEFT // 机器标识部分
                | sequence; // 序列号部分
    }

    private long getNextMill() {
        long mill = getCurrentTimestamp();
        while (mill <= lastStamp) {
            mill = getCurrentTimestamp();
        }
        return mill;
    }

    private long getCurrentTimestamp() {
        return System.currentTimeMillis();
    }

}
