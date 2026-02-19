/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Exception.java to edit this template
 */
package com.xahico.boot.lang.jsox;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public class JSOXException extends Exception {
	/**
	 * Creates a new instance of <code>JSOXException</code> without
	 * detail message.
	**/
	public JSOXException (){
		super();
	}
	
	/**
	 * Constructs an instance of <code>JSOXException</code> with the
	 * specified detail message.
	 *
	 * @param msg 
	 * the detail message.
	**/
	public JSOXException (final String msg){
		super(msg);
	}
}