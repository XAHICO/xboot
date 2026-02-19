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
public class GWXCustomException extends GWXException {
	public GWXCustomException (final GWXStatus status, final String msg){
		super(status, msg);
	}
	
	public GWXCustomException (final GWXStatus status, final String msg, final Throwable cause){
		super(status, msg, cause);
	}
	
	public GWXCustomException (final GWXStatus status, final Throwable cause){
		super(status, cause);
	}
}