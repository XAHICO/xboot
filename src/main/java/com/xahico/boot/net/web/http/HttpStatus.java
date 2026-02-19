/*
 * Written by Tuomas Kontiainen <https://www.github.com/tutomiko>
 * 
 * Copyright (c) 2023, Tuomas Kontiainen
 */
package com.xahico.boot.net.web.http;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public enum HttpStatus {
	STATUS_FORBIDDEN(403),
	STATUS_NOT_FOUND(404),
	STATUS_OK(200),
	STATUS_REDIRECT(302),
	STATUS_UNAUTHORIZED(401);
	
	
	
	public static HttpStatus forCode (final int code) throws IllegalArgumentException {
		for (final var status : HttpStatus.values()) {
			if (status.code == code) {
				return status;
			}
		}
		
		throw new IllegalArgumentException("No such %s: %d".formatted(HttpStatus.class.getSimpleName(), code));
	}
	
	
	
	private final int code;
	
	
	
	HttpStatus (final int code){
		this.code = code;
	}
	
	
	
	public int code (){
		return this.code;
	}
}