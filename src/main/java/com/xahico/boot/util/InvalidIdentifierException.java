/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Exception.java to edit this template
 */
package com.xahico.boot.util;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public class InvalidIdentifierException extends Exception {
	/**
	 * Creates a new instance of <code>InvalidIdentifierException</code> without
	 * detail message.
	**/
	public InvalidIdentifierException (){
		super();
	}
	
	/**
	 * Constructs an instance of <code>InvalidIdentifierException</code> with the
	 * specified detail message.
	 *
	 * @param msg 
	 * the detail message.
	**/
	public InvalidIdentifierException (final String msg){
		super(msg);
	}
}