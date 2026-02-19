/*
 * Written by Tuomas Kontiainen <https://www.github.com/tutomiko>
 * 
 * Copyright (c) 2023, Tuomas Kontiainen
 */
package com.xahico.boot.net.inet;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public class NotDivisibleException extends Exception {
	/**
	 * Creates a new instance of <code>NotDivisibleException</code> without
	 * detail message.
	**/
	public NotDivisibleException (){
		super();
	}
	
	/**
	 * Constructs an instance of <code>NotDivisibleException</code> with the
	 * specified detail message.
	 *
	 * @param msg 
	 * the detail message.
	**/
	public NotDivisibleException (final String msg){
		super(msg);
	}
}