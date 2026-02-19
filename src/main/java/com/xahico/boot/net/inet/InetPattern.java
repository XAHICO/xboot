/*
 * Written by Tuomas Kontiainen <https://www.github.com/tutomiko>
 * 
 * Copyright (c) 2023, Tuomas Kontiainen
 */
package com.xahico.boot.net.inet;

import java.net.InetAddress;

/**
 * {@code InetPattern}, used in subnet matching.
 * 
 * @author Tuomas Kontiainen
**/
public abstract class InetPattern {
	protected static final int ANY_BYTE = -1;
	public static final String TOKEN = "x";
	public static final char   TOKEN_CHAR = 'x';
	
	
	
	public static InetPattern getInetPattern (final String pattern) throws InvalidPatternException {
		if (Internet4Pattern.validateInet4Pattern(pattern)) 
			return Internet4Pattern.getInet4Pattern(pattern);
		else {
			throw new InvalidPatternException(String.format(""));
		}
	}
	
	
	
	public InetPattern (){
		super();
	}
	
	
	
	public abstract String getPattern ();
	
	public abstract boolean matches (final byte[] addressBytes);
	
	public abstract boolean matches (final InetAddress address);
	
	@Override
	public String toString (){
		return String.format("%s [%s]", this.getClass().getSimpleName(), this.getPattern());
	}
}