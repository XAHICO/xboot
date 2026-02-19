/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.publish;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public class GWXException extends Exception {
	private final GWXStatus status;
	
	
	
	public GWXException (final GWXStatus status){
		super();
		
		this.status = status;
	}
	
	public GWXException (final GWXStatus status, final String msg){
		super(msg);
		
		this.status = status;
	}
	
	public GWXException (final GWXStatus status, final Throwable cause){
		super(cause);
		
		this.status = status;
	}
	
	public GWXException (final GWXStatus status, final String msg, final Throwable cause){
		super(msg, cause);
		
		this.status = status;
	}
	
	
	
	public GWXStatus status (){
		return this.status;
	}
}