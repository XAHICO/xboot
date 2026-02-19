/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.util;

import java.util.UUID;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class IDFactory {
	private static final int IDENTIFIER_LENGTH = IDFactory.generateIdentifier().length();
	
	
	
	public static String generateIdentifier (){
		final StringBuilder sb;
		final String        sid;
		
		sid = UUID.randomUUID().toString();
		
		sb = new StringBuilder();
		
		for (var i = 0; i < sid.length(); i++) {
			final char c;
			
			c = sid.charAt(i);
			
			if (c == '-') 
				continue;
			
			sb.append(c);
		}
		
		return sb.toString();
	}
	
	public static int generationLength (){
		return IDENTIFIER_LENGTH;
	}
	
	public static boolean validateIdentifier (final String identifier){
		return ((null != identifier) && (identifier.length() == IDFactory.generationLength()));
	}
	
	
	
	private IDFactory (){
		throw new UnsupportedOperationException("Not supported.");
	}
}