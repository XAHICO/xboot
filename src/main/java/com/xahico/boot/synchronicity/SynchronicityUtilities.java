/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.synchronicity;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class SynchronicityUtilities {
	public static boolean await (final ManagedExecutor executor, final List<Promise> futures, final Runnable callback){
		boolean completed = true;
		
		for (final var future : futures) {
			if (! future.isComplete()) {
				completed = false;
				
				break;
			}
		}
		
		if (! completed) {
			executor.call(() -> await(executor, futures, callback));
			
			return false;
		} else {
			callback.run();
			
			return true;
		}
	}
	
	public static <T> Future<T> bindFuture (final AtomicReference<T> reference){
		return bindFuture(reference, reference);
	}
	
	public static <T> Future<T> bindFuture (final AtomicReference<T> reference, final Object mutex){
		return new Future<>() {
			boolean canceled = false;
			boolean done = false;
			
			@Override
			public boolean cancel (final boolean mayInterruptIfRunning){
				if (this.done || this.canceled) 
					return false;
				else {
					this.canceled = true;
					
					return true;
				}
			}
			
			@Override
			public boolean isCancelled() {
				return this.canceled;
			}
			
			@Override
			public boolean isDone (){
				return this.done;
			}
			
			@Override
			public T get () throws InterruptedException {
				synchronized (mutex) {
					mutex.wait();
					
					return reference.get();
				}
			}
			
			@Override
			public T get (final long timeout, final TimeUnit unit) throws InterruptedException, TimeoutException {
				synchronized (mutex) {
					mutex.wait(unit.toMillis(timeout));
					
					if (!this.done || this.canceled) {
						throw new TimeoutException();
					}
					
					return reference.get();
				}
			}
		};
	}
	
	public static Executor currentThreadExecutor (){
		return Runnable::run;
	}
	
	public static void invoke (final Object mutex){
		synchronized (mutex) {
			mutex.notifyAll();
		}
	}
	
	public static void invoke (final Object mutex, final int count){
		synchronized (mutex) {
			for (var i = 0; i < count; i++) {
				mutex.notify();
			}
		}
	}
	
	public static void invokeOnce (final Object mutex){
		synchronized (mutex) {
			mutex.notify();
		}
	}
	
	public static ControlledExecutor newControlledExecutor (){
		return new ControlledExecutor();
	}
	
	public static ControlledExecutor newControlledExecutor (final int threadCount){
		return new ControlledExecutor(threadCount);
	}
	
	public static ManagedExecutor newManagedMultiThreadExecutor (){
		return new ManagedExecutor((executor) -> Executors.newCachedThreadPool((routine) -> executor.spawn(routine)));
	}
	
	public static ManagedExecutor newManagedMultiThreadExecutor (final int threadCount){
		return new ManagedExecutor((executor) -> Executors.newFixedThreadPool(threadCount, (routine) -> executor.spawn(routine)));
	}
	
	public static ManagedExecutor newManagedSingleThreadExecutor (){
		return new ManagedExecutor((executor) -> Executors.newSingleThreadExecutor((routine) -> executor.spawn(routine)));
	}
	
	public static long waitForObject (final Object mutex, final long timeoutMillis, final int timeoutNanos) throws InterruptedException {
		synchronized (mutex) {
			final long waitBegin;
			final long waitEnd;
			
			waitBegin = System.nanoTime();
			
			mutex.wait(timeoutMillis, timeoutNanos);
			
			waitEnd = System.nanoTime();
			
			return (waitEnd - waitBegin);
		}
	}
	
	public static long waitForObject (final Object mutex, final long timeoutMillis, final int timeoutNanos, final Runnable callback) throws InterruptedException {
		final long wait;
		
		assert(null != callback);
		
		wait = waitForObject(mutex, timeoutMillis, timeoutNanos);
		
		callback.run();
		
		return wait;
	}
	
	public static long waitForObject (final Object mutex, final long timeout, final TimeUnit unit) throws InterruptedException {
		if (unit == TimeUnit.NANOSECONDS) 
			return waitForObject(mutex, 0, (int) unit.toNanos(timeout));
		else {
			return waitForObject(mutex, unit.toMillis(timeout), 0);
		}
	}
	
	public static long waitForObject (final Object mutex, final long timeout, final TimeUnit unit, final Runnable callback) throws InterruptedException {
		final long wait;
		
		assert(null != callback);
		
		wait = waitForObject(mutex, timeout, unit);
		
		callback.run();
		
		return wait;
	}
	
	public static long waitForObject (final Object mutex, final Runnable callback) throws InterruptedException {
		synchronized (mutex) {
			final long waitBegin;
			final long waitEnd;
			
			waitBegin = System.nanoTime();
			
			mutex.wait();
			
			waitEnd = System.nanoTime();
			
			if (null != callback) {
				callback.run();
			}
			
			return (waitEnd - waitBegin);
		}
	}
	
	
	
	private SynchronicityUtilities (){
		throw new UnsupportedOperationException("Not supported.");
	}
}