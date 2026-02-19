/*
 * Written by Tuomas Kontiainen <https://www.github.com/tutomiko>
 * 
 * Copyright (c) 2023, Tuomas Kontiainen
 */
package com.xahico.boot.geo;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public class AddressNotLocatibleException extends Exception {
	/**
	 * Creates a new instance of <code>AddressNotLocatibleException</code> 
	 * without detail message.
	**/
	public AddressNotLocatibleException (){
		super();
	}
	
	/**
	 * Constructs an instance of <code>AddressNotLocatibleException</code> 
	 * with the specified detail message.
	 *
	 * @param msg 
	 * the detail message.
	**/
	public AddressNotLocatibleException (final String msg){
		super(msg);
	}
}