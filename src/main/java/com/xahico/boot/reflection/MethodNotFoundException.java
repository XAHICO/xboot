/*
 * Written by Tuomas Kontiainen <https://www.github.com/tutomiko>
 * 
 * Copyright (c) 2023, Tuomas Kontiainen
 */
package com.xahico.boot.reflection;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public class MethodNotFoundException extends Exception {
	/**
	 * Constructs an instance of <code>NoSuchMethodException</code> with the
	 * specified detail message.
	 *
	 * @param msg 
	 * the detail message.
	**/
	public MethodNotFoundException (final String msg){
		super(msg);
	}
}