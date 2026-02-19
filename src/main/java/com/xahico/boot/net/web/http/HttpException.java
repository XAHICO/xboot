/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.net.web.http;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public class HttpException extends Exception {
	private final HttpStatus status;
	
	
	
	public HttpException (final HttpStatus status, final String message){
		super(message);
		
		this.status = status;
	}
	
	
	
	public final HttpStatus status (){
		return this.status;
	}
}