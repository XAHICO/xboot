/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.distributed.identification;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

/**
 * High-performance, lock-free Chronologically Unique Identifier (CUID).
 * 
 * @author Tuomas Kontiainen
**/
final class FastCUIDFactory {
	private static final long ONE_MILLION_NANOSECONDS = 1_000_000L;
	
	// ThreadLocal random source (fast)
	private static final ThreadLocal<ThreadLocalRandom> RANDOM = ThreadLocal.withInitial(ThreadLocalRandom::current);
	
	
	
	public static UUID random(){
		return Singleton.INSTANCE.generate();
	}
	
	
	
	private final AtomicLong counter = new AtomicLong(0);
	// Track last timestamp in nanos, allows multiple IDs per nano
	private final AtomicLong lastTime = new AtomicLong(0);
	
	
	
	public FastCUIDFactory (){
		super();
	}
	
	
	
	public UUID generate() {
		long nowNanos = System.currentTimeMillis() * ONE_MILLION_NANOSECONDS + System.nanoTime() % ONE_MILLION_NANOSECONDS;

		// increment counter if same timestamp
		long stamp = lastTime.updateAndGet(prev -> nowNanos <= prev ? prev + 1 : nowNanos);
		long count = counter.getAndIncrement();

		// Combine timestamp + counter with random for low collision risk
		long timePart = stamp;
		long randPart = (RANDOM.get().nextLong() & 0x0000_FFFF_FFFF_FFFFL) | (count << 48); // 16-bit counter + 48-bit random

		return new UUID(timePart, randPart);
	}
	
	
	
	private static interface Singleton {
		FastCUIDFactory INSTANCE = new FastCUIDFactory();
	}
}