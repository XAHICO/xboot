/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Exception.java to edit this template
 */
package com.xahico.boot.lang.html;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public class HTMLException extends Exception {
	/**
	 * Creates a new instance of <code>HTMLException</code> without detail
	 * message.
	**/
	public HTMLException (){
		super();
	}
	
	/**
	 * Constructs an instance of <code>HTMLException</code> with the specified
	 * detail message.
	 *
	 * @param msg 
	 * the detail message.
	**/
	public HTMLException (final String msg){
		super(msg);
	}
}