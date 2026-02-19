/*
 * Written by Tuomas Kontiainen <https://www.github.com/tutomiko>
 * 
 * Copyright (c) 2023, Tuomas Kontiainen
 */
package com.xahico.boot.pilot;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public class NoSuchItemException extends Exception {
	public NoSuchItemException (){
		super();
	}
	
	public NoSuchItemException (final String msg){
		super(msg);
	}
}