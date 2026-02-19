/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.pilot;

import com.xahico.boot.util.Exceptions;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
@AutoStart
@Dependencies({})
public final class Core extends ThreadCore {
	private static final int PARALLELLISM_ASYNC = 2;
	private static final int PARALLELLISM_BLOCKING = 3;
	
	
	private static volatile ExecutorService executorAsync = Executors.newWorkStealingPool(Runtime.getRuntime().availableProcessors() * PARALLELLISM_ASYNC);
	private static volatile ExecutorService executorIO = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * PARALLELLISM_BLOCKING, (r) -> {
			Thread t = new Thread(r);
			t.setName("%s-io-%s".formatted(Core.class.getSimpleName().toLowerCase(), t.getId()));
			return t;
		});
	
	
	
	public static void executeAsync (final Runnable runnable){
		executorAsync.execute(runnable);
	}
	
	public static void executeBlocking (final Runnable runnable){
		executorIO.execute(runnable);
	}
	
	public static ExecutorService getAsyncExecutor (){
		return executorAsync;
	}
	
	public static ExecutorService getIOExecutor (){
		return executorIO;
	}
	
	public static int getOptimalParallellism (){
		return (Runtime.getRuntime().availableProcessors() * 4);
	}
	
	public static void scheduleAsync (final Runnable runnable, final long time, final TimeUnit timeUnit){
		ThreadCore.schedule(() -> executeAsync(runnable), time, timeUnit);
	}
	
	public static void scheduleBlocking (final Runnable runnable, final long time, final TimeUnit timeUnit){
		ThreadCore.schedule(() -> executeBlocking(runnable), time, timeUnit);
	}
	
	@Export(ExportType.START)
	static void start (){
		System.out.println("Initialized Core");
	}
	
	@Export(ExportType.STOP)
	static void stop (){
		executorAsync.shutdown();
		executorIO.shutdown();
		
		try {
			executorAsync.awaitTermination(Integer.MAX_VALUE, TimeUnit.MILLISECONDS);
		} catch (final InterruptedException ex) {
			Exceptions.ignore(ex);
			
			executorAsync.shutdownNow();
		}
		
		try {
			executorIO.awaitTermination(Integer.MAX_VALUE, TimeUnit.MILLISECONDS);
		} catch (final InterruptedException ex) {
			Exceptions.ignore(ex);
			
			executorIO.shutdownNow();
		}
	}
	
	
	
	private Core (){
		throw new UnsupportedOperationException("Not supported.");
	}
}