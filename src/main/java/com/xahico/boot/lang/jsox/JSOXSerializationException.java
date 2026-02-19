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
public class JSOXSerializationException extends RuntimeException {
	public JSOXSerializationException (final String msg, final Throwable cause){
		super(msg, cause);
	}
	
	public JSOXSerializationException (final Throwable cause){
		super(cause);
	}
}