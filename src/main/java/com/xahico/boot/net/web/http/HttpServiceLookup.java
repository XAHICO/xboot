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
final class HttpServiceLookup {
	private final String path;
	
	
	
	public static String formatPathString (final String path){
		final StringBuilder sb;
		
		sb = new StringBuilder();
		sb.append('/');
		
		for (var i = 0; i < path.length(); i++) {
			char c;
			
			c = path.charAt(i);
			
			if (c == '\\') 
				c = '/';
			
			if ((c == '/') && ((sb.length() > 0) && (sb.charAt(i - 1) == '/'))) 
				continue;
			
			sb.append(c);
		}
		
		return sb.toString();
	}
	
	public static HttpServiceLookup lookup (final String path){
		return new HttpServiceLookup(formatPathString(path));
	}
	
	
	
	private HttpServiceLookup (final String path){
		super();
		
		this.path = path;
	}
	
	
	
	public boolean hasRoot (){
		final int delimiter;
		
		delimiter = this.path.indexOf('/', 1);
		
		return (delimiter != -1);
	}
	
	public String item (){
		final int delimiter;
		
		delimiter = this.path.lastIndexOf('/');
		
		if (delimiter == -1) {
			if (this.path.length() > 1) 
				return this.path.substring(1);
			else {
				return "";
			}
		} else {
			return this.path.substring(delimiter + 1);
		}
	}
	
	public String root (){
		final int delimiter;
		
		delimiter = this.path.indexOf('/', 1);
		
		if (delimiter == -1) 
			return "/";
		else {
			return this.path.substring(1, delimiter);
		}
	}
	
	@Override
	public String toString (){
		return this.path;
	}
}