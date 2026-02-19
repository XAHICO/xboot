/*
 * Written by Tuomas Kontiainen <https://www.github.com/tutomiko>
 * 
 * Copyright (c) 2023, Tuomas Kontiainen
 */
package com.xahico.boot.net.web.http;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public class InvalidMethodException extends Exception {
	/**
	 * Constructs an instance of <code>InvalidMethodException</code> with
	 * the specified detail message.
	 *
	 * @param msg 
	 * the detail message.
	**/
	public InvalidMethodException (final String msg){
		super(msg);
	}
}