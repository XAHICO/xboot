/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.distributed.identification;

/**
 * TBD.
 * 
 * @author ChatGPT-5
**/
public final class SafeSnowflake {
    private static final long EPOCH = 1577836800000L; // 2020-01-01

    private static final long WORKER_ID_BITS = 5;
    private static final long DATA_CENTER_BITS = 5;
    private static final long SEQUENCE_BITS = 12;

    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);
    private static final long MAX_DATA_CENTER_ID = ~(-1L << DATA_CENTER_BITS);

    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
    private static final long DATA_CENTER_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;
    private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATA_CENTER_BITS;

    private final long workerId;
    private final long dataCenterId;

    private long sequence = 0;
    private long lastTimestamp = -1;

    public SafeSnowflake(long workerId, long dataCenterId) {
        if (workerId > MAX_WORKER_ID || workerId < 0)
            throw new IllegalArgumentException("workerId out of range");
        if (dataCenterId > MAX_DATA_CENTER_ID || dataCenterId < 0)
            throw new IllegalArgumentException("dataCenterId out of range");

        this.workerId = workerId;
        this.dataCenterId = dataCenterId;
    }

    public synchronized long nextId() {
        long timestamp = System.currentTimeMillis();

        if (timestamp < lastTimestamp) {
            // Clock moved backwards: wait until lastTimestamp or move forward
            long offset = lastTimestamp - timestamp;
            try {
                Thread.sleep(offset);
                timestamp = System.currentTimeMillis();
                if (timestamp < lastTimestamp) {
                    // Still behind: force forward
                    timestamp = lastTimestamp;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while waiting for clock to catch up", e);
            }
        }

        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & ((1 << SEQUENCE_BITS) - 1);
            if (sequence == 0) {
                // Sequence exhausted in this millisecond: wait for next millisecond
                timestamp = waitNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0;
        }

        lastTimestamp = timestamp;

        return ((timestamp - EPOCH) << TIMESTAMP_SHIFT)
                | (dataCenterId << DATA_CENTER_SHIFT)
                | (workerId << WORKER_ID_SHIFT)
                | sequence;
    }

    private long waitNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }
}