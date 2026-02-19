/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.synchronicity;

import java.util.concurrent.TimeoutException;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class Mutex {
	private boolean alertOccurred = false;
	
	
	
	public Mutex (){
		super();
	}
	
	
	
	public synchronized void alert (){
		alertOccurred = true;
	}
	
	public synchronized void reset (){
		alertOccurred = false;
	}
	
	@SuppressWarnings({"SleepWhileHoldingLock","SleepWhileInLoop"})
	public long waitAlert () throws InterruptedException {
		final long waitBegin;
		final long waitEnd;
		
		waitBegin = System.nanoTime();
		
		for (;;) synchronized (Mutex.this) {
			if (this.alertOccurred) {
				break;
			}
			
			Thread.sleep(0, 100);
		}
		
		waitEnd = System.nanoTime();
		
		return (waitEnd - waitBegin);
	}
	
	@SuppressWarnings({"SleepWhileHoldingLock","SleepWhileInLoop"})
	public long waitAlert (final long timeoutMillis) throws InterruptedException, TimeoutException {
		final long waitBegin;
		final long waitEnd;
		
		waitBegin = System.currentTimeMillis();
		
		for (;;) synchronized (Mutex.this) {
			if (this.alertOccurred) {
				break;
			}
			
			if ((System.currentTimeMillis() - waitBegin) > timeoutMillis) {
				throw new TimeoutException();
			}
			
			Thread.sleep(1);
		}
		
		waitEnd = System.nanoTime();
		
		return (waitEnd - waitBegin);
	}
}