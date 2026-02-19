/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.synchronicity;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class ControlledExecutor implements ExecutionHandler {
	private final ManagedExecutor executorAsync;
	private final ManagedExecutor executorEsync = SynchronicityUtilities.newManagedSingleThreadExecutor();
	
	
	
	ControlledExecutor (){
		super();
		
		this.executorAsync = SynchronicityUtilities.newManagedMultiThreadExecutor();
	}
	
	ControlledExecutor (final int threadCount){
		super();
		
		this.executorAsync = SynchronicityUtilities.newManagedMultiThreadExecutor(threadCount);
	}
	
	
	
	@Override
	public void call (final Runnable routine){
		this.executorAsync.call(routine);
	}
	
	public Promise executeAsync (final Runnable routine){
		return this.executeAsync(routine, null);
	}
	
	public Promise executeAsync (final Runnable routine, final Runnable callback){
		final Promise future;
		
		future = new Promise(callback);
		
		this.executeAsyncInternal(() -> {
			try {
				routine.run();
			} finally {
				future.complete();
			}
		});
		
		return future;
	}
	
	private boolean executeAsyncInternal (final Runnable routine){
		if (this.executorAsync.isOwnerOfThread(Thread.currentThread())) {
			routine.run();
			
			return true;
		} else {
			this.executorAsync.callops(() -> this.executeAsyncInternal(routine));
			
			return false;
		}
	}
	
	public void executeEsync (final Runnable routine){
		if (this.executorEsync.isOwnerOfThread(Thread.currentThread())) {
			routine.run();
		} else {
			this.executorAsync.suspend();

			this.executorEsync.call(() -> {
				try {
					routine.run();
				} finally {
					this.executorAsync.resume();
				}
			});
		}
	}
	
	public ManagedExecutor getAsyncExecutor (){
		return this.executorAsync;
	}
	
	public ManagedExecutor getEsyncExecutor (){
		return this.executorEsync;
	}
	
	@Override
	public boolean isOwnerOfThread (final long threadId){
		return this.executorAsync.isOwnerOfThread(threadId);
	}
	
	public void panic (){
		this.executorAsync.panic();
		this.executorEsync.panic();
	}
	
	public void resume (){
		this.executorAsync.resume();
		this.executorEsync.resume();
	}
	
	public void suspend (){
		this.executorAsync.suspend();
		this.executorEsync.suspend();
	}
}