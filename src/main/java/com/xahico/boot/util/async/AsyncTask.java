/*
 * Written by Tuomas Kontiainen <https://www.github.com/tutomiko>
 * 
 * Copyright (c) 2023, Tuomas Kontiainen
 */
package com.xahico.boot.util.async;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * ...
 * 
 * @param <T> 
 * ...
 * 
 * @author Tuomas Kontiainen
**/
public final class AsyncTask <T> {
	private final Condition     condition;
	private final ReentrantLock lock;
	private volatile boolean    executed = false;
	private volatile boolean    failed = false;
	private volatile int        futuresWaiting = 0;
	private volatile T          returnValue = null;
	private final Callable<T>   routine;
	private Throwable           thrownException = null;



	public AsyncTask (final Callable<T> routine){
		super();

		this.routine = routine;
		this.lock = new ReentrantLock();
		this.condition = this.lock.newCondition();
	}

	
	
	public boolean failed (){
		return this.failed;
	}
	
	public boolean finished (){
		return this.executed;
	}

	public Future<T> future (){
		futuresWaiting++;
		
		return new Future<>() {
			boolean canceled = false;
			
			@Override
			public void cancel (){
				if (! this.canceled) {
					this.canceled = true;
					
					AsyncTask.this.futuresWaiting--;
				}
			}
			
			@Override
			public boolean failed (){
				return AsyncTask.this.failed();
			}
			
			@Override
			public boolean isCanceled (){
				return this.canceled;
			}
			
			@Override
			public boolean isDone (){
				return AsyncTask.this.finished();
			}
			
			@Override
			public T get () throws InterruptedException {
				if (! AsyncTask.this.finished()) {
					try {
						lock.lock();
						
						condition.await();
					} finally {
						lock.unlock();
					}
				}
				
				return AsyncTask.this.returnValue;
			}
			
			@Override
			public T get (final long timeoutMillis) throws InterruptedException, TimeoutException {
				return this.get(timeoutMillis, TimeUnit.MILLISECONDS);
			}

			@Override
			public T get (final long timeout, final TimeUnit timeUnit) throws InterruptedException, TimeoutException {
				if (! AsyncTask.this.finished()) {
					try {
						lock.lock();

						if (! condition.await(timeout, timeUnit)) {
							throw new TimeoutException("Timed out");
						}
					} finally {
						lock.unlock();
					}
				}

				return AsyncTask.this.returnValue;
			}

			@Override
			public boolean succeeded (){
				return AsyncTask.this.succeeded();
			}
			
			@Override
			public Throwable thrownException (){
				return AsyncTask.this.thrownException();
			}
		};
	}
	
	public boolean isFuturesWaiting (){
		return (this.futuresWaiting > 0);
	}

	@SuppressWarnings("UseSpecificCatch")
	public void run (){
		synchronized (AsyncTask.this) {
			try {
				AsyncTask.this.returnValue = AsyncTask.this.routine.call();
			} catch (final Throwable t) {
				this.thrownException = t;
				this.failed = true;
			} finally {
				this.executed = true;
				
				try {
					this.lock.lock();
					
					this.condition.signalAll();
				} finally {
					this.lock.unlock();
				}
			}
		}
	}
	
	public boolean succeeded (){
		return !this.failed;
	}
	
	public Throwable thrownException (){
		return this.thrownException;
	}
	
	
	
	public static interface Future <T> {
		void cancel ();
		
		boolean failed ();
		
		boolean isDone ();
		
		T get () throws InterruptedException;
		
		T get (final long timeoutMillis) throws InterruptedException, TimeoutException;
		
		T get (final long timeout, final TimeUnit timeUnit) throws InterruptedException, TimeoutException;
		
		boolean isCanceled ();
		
		boolean succeeded ();
		
		Throwable thrownException ();
	}
}