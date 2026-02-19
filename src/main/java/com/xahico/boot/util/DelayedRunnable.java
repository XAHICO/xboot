/*
 * Written by Tuomas Kontiainen <https://www.github.com/tutomiko>
 * 
 * Copyright (c) 2023, Tuomas Kontiainen
 */
package com.xahico.boot.util;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class DelayedRunnable {
	private Runnable runnable = null;
	
	
	
	public DelayedRunnable (){
		super();
	}
	
	
	
	public Runnable getRunnable (){
		return this.runnable;
	}
	
	public void run (){
		if (null != this.runnable) {
			this.runnable.run();
		}
	}
	
	public void setRunnable (final Runnable runnable){
		this.runnable = runnable;
	}
}