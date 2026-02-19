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
public class EndOfDocumentException extends HTMLException {
	/**
	 * Creates a new instance of <code>EndOfDocumentException</code> without detail
	 * message.
	**/
	public EndOfDocumentException (){
		super();
	}
	
	/**
	 * Constructs an instance of <code>EndOfDocumentException</code> with the specified
	 * detail message.
	 *
	 * @param msg 
	 * the detail message.
	**/
	public EndOfDocumentException (final String msg){
		super(msg);
	}
}