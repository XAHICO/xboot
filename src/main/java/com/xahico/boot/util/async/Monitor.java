/*
 * Written by Tuomas Kontiainen <https://www.github.com/tutomiko>
 * 
 * Copyright (c) 2023, Tuomas Kontiainen
 */
package com.xahico.boot.util.async;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
@FunctionalInterface
public interface Monitor {
	void enterState (final State state);
	
	
	
	public static enum State {
		DEAD,
		
		DYING,
		
		IDLE,
		
		RUNNING,
		
		STARTING,
		
		WAKEUP
	}
}