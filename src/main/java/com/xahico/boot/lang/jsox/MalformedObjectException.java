/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Exception.java to edit this template
 */
package com.xahico.boot.lang.jsox;

import com.xahico.boot.net.sock.model.bash.BASHException;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public class MalformedObjectException extends JSOXException {
	/**
	 * Creates a new instance of <code>MalformedObjectException</code> without
	 * detail message.
	**/
	public MalformedObjectException (){
		super();
	}
	
	/**
	 * Constructs an instance of <code>MalformedObjectException</code> with the
	 * specified detail message.
	 *
	 * @param msg 
	 * the detail message.
	**/
	public MalformedObjectException (final String msg){
		super(msg);
	}
}