/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Exception.java to edit this template
 */
package com.xahico.boot.net.inet;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public class InvalidRangeException extends RuntimeException {
	/**
	 * Creates a new instance of <code>InvalidRangeException</code> without
	 * detail message.
	**/
	public InvalidRangeException (){
		super();
	}
	
	/**
	 * Constructs an instance of <code>InvalidRangeException</code> with the
	 * specified detail message.
	 *
	 * @param msg 
	 * the detail message.
	**/
	public InvalidRangeException (final String msg){
		super(msg);
	}
}