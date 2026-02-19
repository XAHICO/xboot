/*
 * Written by Tuomas Kontiainen <https://www.github.com/tutomiko>
 * 
 * Copyright (c) 2023, Tuomas Kontiainen
 */
package com.xahico.boot.net;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public class InvalidControlException extends Exception {
	/**
	 * Constructs an instance of <code>InvalidControlException</code> with the
	 * specified detail message.
	 *
	 * @param msg 
	 * the detail message.
	**/
	public InvalidControlException (final String msg){
		super(msg);
	}
}