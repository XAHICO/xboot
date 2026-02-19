/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.xahico.boot.util.configurable;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class NoSuchPropertyException extends Exception {
	/**
	 * Creates a new instance of <code>NoSuchFieldException</code> without detail message.
	**/
	public NoSuchPropertyException (){
		super();
	}
	
	/**
	 * Constructs an instance of <code>NoSuchFieldException</code> with the specified detail message.
	 *
	 * @param msg 
	 * the detail message.
	**/
	public NoSuchPropertyException (final String msg){
		super(msg);
	}
}