/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.pilot;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public abstract class ThreadCore {
	private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	
	
	
	public static long clock (){
		return System.nanoTime();
	}
	
	public static Routine createRoutine (final Runnable runnable, final long time, final TimeUnit timeUnit){
		return new Routine(runnable, time, timeUnit);
	}
	
	public static void schedule (final Runnable runnable, final long time, final TimeUnit timeUnit){
		scheduler.schedule(runnable, time, timeUnit);
	}
	
	@Export(ExportType.STOP)
	static void shutdown (){
		
	}
	
	@Export(ExportType.START)
	static void start (){
		
	}
	
	
	
	protected ThreadCore (){
		throw new UnsupportedOperationException("Not supported.");
	}
	
	
	
	public static final class Routine {
		private volatile Future<?> future = null;
		private final Runnable     runnable;
		private final long         time;
		private final TimeUnit     timeUnit;
		
		
		
		private Routine (final Runnable runnable, final long time, final TimeUnit timeUnit){
			super();
			
			this.time = time;
			this.timeUnit = timeUnit;
			this.runnable = runnable;
		}
		
		
		
		private void run (){
			this.runnable.run();
		}
		
		public void start (){
			this.future = scheduler.scheduleAtFixedRate(() -> this.run(), this.time, this.time, this.timeUnit);
		}
		
		public void stop (){
			if (null != this.future) {
				this.future.cancel(true);
			}
		}
	}
}