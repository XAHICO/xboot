/*
 * Written by Tuomas Kontiainen <https://www.github.com/tutomiko>
 * 
 * Copyright (c) 2023, Tuomas Kontiainen
 */
package com.xahico.boot.pilot;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public class InitializeException extends Exception {
	public InitializeException (final String message){
		super(message);
	}
	
	public InitializeException (final String message, final Throwable cause){
		super(message, cause);
	}
	
	public InitializeException (final Throwable cause){
		super(cause);
	}
}