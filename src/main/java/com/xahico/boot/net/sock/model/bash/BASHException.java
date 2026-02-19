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
public class BASHException extends Exception {
	public BASHException (){
		super();
	}
	
	public BASHException (final String message){
		super(message);
	}
	
	public BASHException (final String message, final Throwable cause){
		super(message, cause);
	}
	
	public BASHException (final Throwable cause){
		super(cause);
	}
}