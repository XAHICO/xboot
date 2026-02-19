/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.synchronicity;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public class SynchronizationHandler implements Synchronizable {
	private Executor executor;
	
	
	
	@SuppressWarnings("OverridableMethodCallInConstructor")
	protected SynchronizationHandler (){
		super();
		
		this.executor = createDefaultExecutor();
	}
	
	protected SynchronizationHandler (final Executor executor){
		super();
		
		this.executor = executor;
	}
	
	
	
	@Override
	public final void call (final Runnable routine){
		this.executor.execute(routine);
	}
	
	@Override
	public final void call (final ScheduledRunnable routine){
		this.call(new Runnable() {
			@Override
			public void run (){
				if (routine.canRun()) 
					routine.run();
				else {
					SynchronizationHandler.this.call(this);
				}
			}
		});
	}
	
	@Override
	public final <T> Future<T> call (final Callable<T> task){
		if (null == this.executor) 
			throw new Error("executor not set for %s".formatted(this));
		else if (this.executor instanceof ExecutorService) 
			return ((ExecutorService)this.executor).submit(task);
		else {
			throw new UnsupportedOperationException("Not supported: executor of %s is not an instance of %s".formatted(this, ExecutorService.class));
		}
	}
	
	protected Executor createDefaultExecutor (){
		return null;
	}
	
	@Override
	public final Executor getExecutor (){
		return this.executor;
	}
	
	@Override
	public void setExecutor (final Executor executor){
		if (null != executor) 
			this.executor = executor;
		else {
			this.executor = this.createDefaultExecutor();
		}
	}
	
	public final void setExecutor (final SynchronizationProvider provider){
		this.setExecutor(provider.getExecutor());
	}
}