/*
 * Written by Tuomas Kontiainen <https://www.github.com/tutomiko>
 * 
 * Copyright (c) 2023, Tuomas Kontiainen
 */
package com.xahico.boot.net;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class URIAQueryParameter {
	private final String key;
	private final String value;
	
	
	
	URIAQueryParameter (final String key, final String value){
		super();
		
		this.key = key;
		this.value = value;
	}
	
	
	
	@Override
	public String toString (){
		return (this.key + "=" + this.value);
	}
}