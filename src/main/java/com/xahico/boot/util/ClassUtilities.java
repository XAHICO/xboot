/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.xahico.boot.util;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class ClassUtilities {
	public static String abstractClassStringToHumanReadableString (final String string){
		return StringUtilities.capitalizeWords(string.replace('-', ' ').replace('_', ' '), true);
	}
	
	public static String getGenericName (final Class cl){
		return cl.getSimpleName().toLowerCase();
	}
	
	public static String javaClassStringToName (final String string){
		final StringBuilder sb;
		
		sb = new StringBuilder();
		
		for (var i = 0; i < string.length(); i++) {
			final char c;
			
			c = string.charAt(i);
			
			if (Character.isLowerCase(c)) 
				sb.append(c);
			else {
				if ((i > 0) && (!Character.isUpperCase(string.charAt(i - 1)) || Character.isLowerCase(string.charAt(i + 1)))) {
					sb.append(' ');
				}
				
				sb.append(c);
			}
		}
		
		return sb.toString();
	}
	
	
	
	private ClassUtilities (){
		throw new UnsupportedOperationException("Not supported.");
	}
}