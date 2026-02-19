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
public class IncompleteObjectException extends JSOXException {
	/**
	 * Creates a new instance of <code>IncompleteException</code> without
	 * detail message.
	**/
	public IncompleteObjectException (){
		super();
	}
	
	/**
	 * Constructs an instance of <code>IncompleteException</code> with the
	 * specified detail message.
	 *
	 * @param msg 
	 * the detail message.
	**/
	public IncompleteObjectException (final String msg){
		super(msg);
	}
}