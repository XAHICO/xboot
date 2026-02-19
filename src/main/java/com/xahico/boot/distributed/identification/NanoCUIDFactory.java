package com.xahico.boot.distributed.identification;

import com.xahico.boot.pilot.Time;
import java.security.SecureRandom;
import java.util.Random;
import java.util.UUID;

/**
 * Factory for CUIDs (= Chronologically Unique Identifiers.)
 * 
 * @author Tuomas Kontiainen
**/
final class NanoCUIDFactory {
	private static final long ONE_MILLION_NANOSECONDS = 1_000_000L;
	
	private static final ThreadLocal<NanoCUIDFactory> INSTANCE = ThreadLocal.withInitial(() -> NanoCUIDFactory.getSecureFactory());
	
	
	
	public static TimeSource createGlobalTimeSource (){
		return () -> Time.hostTimeMillisNow();
	}
	
	public static TimeSource createLocalTimeSource (){
		return () -> System.currentTimeMillis();
	}
	
	public static RandomSource createPseudoRandomSource (){
		return new RandomSource() {
			Random random = new Random();
			
			@Override
			public long get (){
				return random.nextLong();
			}
		};
	}
	
	public static RandomSource createSecureRandomSource (){
		return new RandomSource() {
			SecureRandom random = new SecureRandom();
			
			@Override
			public long get (){
				return random.nextLong();
			}
		};
	}
	
	public static NanoCUIDFactory getSecureFactory (){
		final NanoCUIDFactory factory;
		
		factory = new NanoCUIDFactory();
		factory.setRandomSource(NanoCUIDFactory.createSecureRandomSource());
		factory.setTimeSource(NanoCUIDFactory.createGlobalTimeSource());
		
		return factory;
	}
	
	public static NanoCUIDFactory getTestFactory (){
		final NanoCUIDFactory factory;
		
		factory = new NanoCUIDFactory();
		factory.setRandomSource(NanoCUIDFactory.createPseudoRandomSource());
		factory.setTimeSource(NanoCUIDFactory.createLocalTimeSource());
		
		return factory;
	}
	
	public static UUID random (){
		synchronized (INSTANCE) {
			return (INSTANCE).get().generate();
		}
	}
	
	
	
	private long         prevMillis = System.currentTimeMillis();
	private long         prevNanos = System.nanoTime();
	private RandomSource randomSource = NanoCUIDFactory.createSecureRandomSource();
	private TimeSource   timeSource = NanoCUIDFactory.createGlobalTimeSource();
	
	
	
	public NanoCUIDFactory (){
		super();
	}
	
	
	
	public synchronized UUID generate (){
		return new UUID(this.getTimeComponent(), this.getRandomComponent());
	}
	
	private long getRandomComponent (){
		return this.randomSource.get();
	}
	
	private long getTimeComponent (){
		long elapsed = 0;
		long timeNanos = System.nanoTime();
		long timeMillis = this.timeSource.get();
		
		if (timeMillis == prevMillis) {
			elapsed = (timeNanos - prevNanos);
		} else {
			prevNanos = timeNanos;
		}
		
		prevMillis = timeMillis;
		
		return ((timeMillis * ONE_MILLION_NANOSECONDS) + elapsed);
	}
	
	public void setRandomSource (final RandomSource source){
		this.randomSource = source;
	}
	
	public void setTimeSource (final TimeSource source){
		this.timeSource = source;
	}
	
	
	
	@FunctionalInterface
	public static interface RandomSource {
		long get ();
	}
	
	@FunctionalInterface
	public static interface TimeSource {
		long get ();
	}
}