/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.synchronicity;

import com.xahico.boot.pilot.ThreadCore;
import com.xahico.boot.util.Exceptions;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class ManagedExecutor implements ExecutionHandler {
	private ExecutorService                      executorService = null;
	private final ManagedExecutorInternalFactory executorServiceFactory;
	private final List<Runnable>                 stack = new LinkedList<>();
	private boolean                              suspended = false;
	private final List<Thread>                   threadpool = new CopyOnWriteArrayList<>();
	
	
	
	ManagedExecutor (final ManagedExecutorInternalFactory executorServiceFactory){
		super();
		
		this.executorServiceFactory = executorServiceFactory;
		this.start();
	}
	
	
	
	@Override
	public void call (final Runnable routine){
		if (this.isSuspended()) {
			this.stack.add(routine);
		} else {
			this.executorService.execute(routine);
		}
	}
	
	public void callDelayed (final Runnable routine, final long time, final TimeUnit timeUnit){
		ThreadCore.schedule(() -> call(routine), time, timeUnit);
	}
	
	private ExecutorService createExecutorService (){
		return this.executorServiceFactory.newInstance(this);
	}
	
	public ExecutorService getService (){
		return this.executorService;
	}
	
	@Override
	public boolean isOwnerOfThread (final long threadId){
		for (final var thread : this.threadpool) {
			if (thread.getId() == threadId) {
				return true;
			}
		}
		
		return false;
	}
	
	public boolean isSuspended (){
		return this.suspended;
	}
	
	public void panic (){
		for (final var thread : this.threadpool) {
			if (null != thread) {
				thread.interrupt();
			}
		}
	}
	
	public void resume () throws IllegalStateException {
		if (! this.isSuspended()) {
			throw new IllegalStateException();
		}

		this.suspended = false;
		
		this.executorService = this.createExecutorService();
		
		if (! this.stack.isEmpty()) {
			this.stack.forEach(routine -> {
				try {
					this.executorService.execute(routine);
				} catch (final Throwable t) {
					Exceptions.ignore(t);
				}
			});
			
			this.stack.clear();
		}
	}
	
	Thread spawn (final Runnable routine){
		final Thread thread;
		
		thread = new Thread(routine);
		
		this.threadpool.add(thread);
		
		return thread;
	}
	
	void start (){
		this.executorService = this.createExecutorService();
	}
	
	public void suspend () throws IllegalStateException {
		try {
			if (this.isSuspended()) {
				throw new IllegalStateException();
			}

			this.suspended = true;
			
			this.stack.addAll(this.executorService.shutdownNow());
			
			if (! this.executorService.isTerminated()) {
				this.executorService.awaitTermination(Integer.MAX_VALUE, TimeUnit.MILLISECONDS);
			}
		} catch (final InterruptedException ex) {
			throw new Error(ex);
		}
	}
	
	public int threadCount (){
		return this.threadpool.size();
	}
}