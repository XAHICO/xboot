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
public interface ExecutionHandler {
	public void call (final Runnable routine);
	
	/**
	 * Executes {@code procedure} immediately if within the 
	 * {@link #isCallingThread() executor thread}, 
	 * otherwise 
	 * {@link #call(java.lang.Runnable) queues} 
	 * it for synchronized execution by the executor thread in the future.
	 * <br>
	 * The given procedure is guaranteed to be executed in synchronicity 
	 * with the executor thread.
	 * 
	 * @param routine 
	 * routine to be executed.
	**/
	default void callops (final Runnable routine){
		if (this.isCallingThread()) {
			routine.run();
		} else {
			this.call(routine);
		}
	}
	
	default boolean isCallingThread (){
		return this.isOwnerOfThread(Thread.currentThread());
	}
	
	public boolean isOwnerOfThread (final long threadId);
	
	default boolean isOwnerOfThread (final Thread thread){
		return this.isOwnerOfThread(thread.getId());
	}
}