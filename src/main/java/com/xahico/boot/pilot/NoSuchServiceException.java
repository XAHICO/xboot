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
public class NoSuchServiceException extends Exception {
	/**
	 * Constructs an instance of <code>NoSuchServiceException</code> with the
	 * specified detail message.
	 *
	 * @param msg 
	 * the detail message.
	**/
	public NoSuchServiceException (final String msg){
		super(msg);
	}
}