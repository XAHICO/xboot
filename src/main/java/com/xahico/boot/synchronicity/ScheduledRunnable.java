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
public final class ScheduledRunnable {
	public static ScheduledRunnable create (final Runnable routine, final ScheduledRunnable.Condition condition){
		return new ScheduledRunnable(routine, condition);
	}
	
	public static ScheduledRunnable create (final Runnable routine, final long waitTimeMillis){
		final long timeMarked;
		
		timeMarked = System.currentTimeMillis();
		
		return new ScheduledRunnable(routine, () -> {
			final long timeNow;
			
			timeNow = System.currentTimeMillis();
			
			return ((timeNow - timeMarked) >= waitTimeMillis);
		});
	}
	
	
	
	private Runnable                          callback = null;
	private final ScheduledRunnable.Condition condition;
	private boolean                           executed = false;
	private final Runnable                    routine;
	
	
	
	private ScheduledRunnable (final Runnable routine, final ScheduledRunnable.Condition condition){
		super();
		
		this.routine = routine;
		this.condition = condition;
	}
	
	
	
	public boolean canRun (){
		return this.condition.check();
	}
	
	public void run (){
		try {
			this.routine.run();
		} finally {
			this.executed = true;
			
			if (null != this.callback) {
				this.callback.run();
			}
		}
	}
	
	public void setCallback (final Runnable callback){
		this.callback = callback;
	}
	
	public boolean wasExecuted (){
		return this.executed;
	}
	
	
	
	public static interface Condition {
		boolean check ();
	}
}