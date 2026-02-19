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
public class UnavailableException extends Exception {
	/**
	 * Creates a new instance of <code>UnavailableException</code> without
	 * detail message.
	**/
	public UnavailableException (){
		super();
	}
	
	/**
	 * Constructs an instance of <code>UnavailableException</code> with the
	 * specified detail message.
	 *
	 * @param msg 
	 * the detail message.
	**/
	public UnavailableException (final String msg){
		super(msg);
	}
}