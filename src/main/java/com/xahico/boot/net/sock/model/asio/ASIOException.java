/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Exception.java to edit this template
 */
package com.xahico.boot.net.sock.model.asio;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public class ASIOException extends Exception {
	public ASIOException (){
		super();
	}
	
	public ASIOException (final String message){
		super(message);
	}
	
	public ASIOException (final String message, final Throwable cause){
		super(message, cause);
	}
	
	public ASIOException (final Throwable cause){
		super(cause);
	}
}