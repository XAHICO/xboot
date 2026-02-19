/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.synchronicity;

import com.xahico.boot.util.Exceptions;
import java.util.concurrent.Executor;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public abstract class SynchronizationProvider extends SynchronizationHandler {
	private ScheduledRunnable clock = null;
	private ClockedIntensity  clockIntensity = ClockedIntensity.MEDIUM;
	private long              clockInterval = 5000;
	private volatile boolean  runClock = false;
	
	
	
	protected SynchronizationProvider (){
		super();
	}
	
	protected SynchronizationProvider (final Executor executor){
		super(executor);
	}
	
	
	
	protected abstract int getCoreCount ();
	
	public abstract boolean isIdle ();
	
	protected void onClock (){
		
	}
	
	private void runClock (){
		try {
			if ((null == clock) || clock.wasExecuted()) {
				clock = ScheduledRunnable.create(this::onClock, this.clockInterval);
			}
			
			if (clock.canRun()) {
				clock.run();
			} else {
				this.sleep();
			}
			
			if (runClock) {
				this.call(this::runClock);
			}
		} catch (final InterruptedException ex) {
			Exceptions.ignore(ex);
		}
	}
	
	public final void setClockInterval (final long intervalMillis){
		this.clockInterval = intervalMillis;
	}
	
	public final void setClockIntensity (final ClockedIntensity clockIntensity){
		this.clockIntensity = clockIntensity;
	}
	
	private void sleep () throws InterruptedException {
		final long waitTime;
		
		if (this.isIdle())
			waitTime = this.clockIntensity.idleClock();
		else {
			waitTime = this.clockIntensity.activeClock(this.getCoreCount());
		}
		
		Thread.sleep(waitTime);
	}
	
	public final void startClock (){
		this.call(() -> runClock = true);
		this.call(this::runClock);
	}
	
	public final void stopClock (){
		this.call(() -> runClock = false);
	}
}