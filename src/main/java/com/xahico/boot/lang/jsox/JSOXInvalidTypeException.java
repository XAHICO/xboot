/*
 * Written by Tuomas Kontiainen <https://www.github.com/tutomiko>
 * 
 * Copyright (c) 2023, Tuomas Kontiainen
 */
package com.xahico.boot.lang.jsox;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public class JSOXInvalidTypeException extends RuntimeException {
	/**
	 * Constructs an instance of <code>UncastableTypeException</code> with the
	 * specified detail message.
	 *
	 * @param msg 
	 * the detail message.
	**/
	public JSOXInvalidTypeException (final String msg){
		super(msg);
	}
}