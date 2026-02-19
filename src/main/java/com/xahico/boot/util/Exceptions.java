/*
 * Written by Tuomas Kontiainen <https://www.github.com/tutomiko>
 * 
 * Copyright (c) 2023, Tuomas Kontiainen
 */
package com.xahico.boot.util;

import java.util.concurrent.ExecutionException;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class Exceptions {
	public static void ignore (final Throwable throwable){
		
	}
	
	public static void wrap (final Throwable throwable) throws ExecutionException {
		throw new ExecutionException(throwable);
	}
	
	
	
	private Exceptions (){
		throw new UnsupportedOperationException("Not supported.");
	}
}