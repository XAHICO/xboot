/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.xahico.boot.util;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class IDTokenFactory {
	public static final int     MAX_ID = Integer.MAX_VALUE;
	private static final String MAX_ID_STRING = Integer.toString(MAX_ID);
	private static final char   PREFIX = '#';
	
	
	
	public static int defactorize (final String sid) throws IllegalArgumentException {
		final int    id;
		final String usid;
		
		if (!(sid.length() > 1) || (sid.charAt(0) != PREFIX)) 
			throw new IllegalArgumentException(String.format("Invalid ID token string \'%s\': not prefixed with '%c'", sid, PREFIX));
		
		usid = sid.substring(1);
		
		id = Integer.parseInt(usid);
		
		return id;
	}
	
	public static String factorize (final int id){
		final int           padding;
		final StringBuilder sb;
		final String        sid;
		
		sid = Integer.toString(id);
		
		padding = (MAX_ID_STRING.length() - sid.length());
		
		sb = new StringBuilder();
		sb.append(PREFIX);
		
		for (var i = 0; i < padding; i++) {
			sb.append('0');
		}
		
		sb.append(sid);
		
		return sb.toString();
	}
	
	
	
	private IDTokenFactory (){
		throw new UnsupportedOperationException("Not supported.");
	}
}