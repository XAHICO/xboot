/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Exception.java to edit this template
 */
package com.xahico.boot.net.sock.model.bash;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public class StateError extends Error {
	/**
	 * Creates a new instance of <code>StateError</code> without
	 * detail message.
	**/
	public StateError (){
		super();
	}
	
	/**
	 * Constructs an instance of <code>StateError</code> with the
	 * specified detail message.
	 *
	 * @param msg 
	 * the detail message.
	**/
	public StateError (final String msg){
		super(msg);
	}
}