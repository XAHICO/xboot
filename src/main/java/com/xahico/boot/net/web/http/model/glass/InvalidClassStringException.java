/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Exception.java to edit this template
 */
package com.xahico.boot.net.web.http.model.glass;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public class InvalidClassStringException extends Exception {
	/**
	 * Creates a new instance of <code>InvalidClassStringException</code> without
	 * detail message.
	**/
	public InvalidClassStringException (){
		super();
	}
	
	/**
	 * Constructs an instance of <code>InvalidClassStringException</code> with the
	 * specified detail message.
	 *
	 * @param msg 
	 * the detail message.
	**/
	public InvalidClassStringException (final String msg){
		super(msg);
	}
}