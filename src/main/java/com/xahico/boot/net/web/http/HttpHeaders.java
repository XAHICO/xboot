/*
 * Written by Tuomas Kontiainen <https://www.github.com/tutomiko>
 * 
 * Copyright (c) 2023, Tuomas Kontiainen
 */
package com.xahico.boot.net.web.http;

import com.sun.net.httpserver.Headers;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public interface HttpHeaders {
	public static HttpHeaders wrap (final Headers headers){
		return new HttpHeaders() {
			@Override
			public void add (final String key, final String value){
				headers.add(key, value);
			}
			
			@Override
			public void clear (){
				headers.clear();
			}
			
			@Override
			public List<String> get (final String key){
				return headers.get(key);
			}
			
			@Override
			public String getFirst (final String key){
				final List<String> collection;
				
				collection = this.get(key);
				
				if ((null == collection) || collection.isEmpty()) 
					return Objects.toString(null);
				
				return collection.get(0);
			}
			
			@Override
			public String getLast (final String key){
				final List<String> collection;
				
				collection = this.get(key);
				
				if ((null == collection) || collection.isEmpty()) 
					return Objects.toString(null);
				
				return collection.get(collection.size() - 1);
			}
			
			@Override
			public Set<String> keySet (){
				final Set<String> collection;
				
				collection = new HashSet<>();
				
				headers.forEach((key, values) -> collection.add(key));
				
				return collection;
			}
			
			@Override
			public int size (){
				return headers.size();
			}
		};
	}
	
	
	
	public void add (final String key, final String value);
	
	public void clear ();
	
	public List<String> get (final String key);
	
	default String getCookie (final String cookie){
		final List<String> collection;
		final String       cookieFull;
		final int          delimiter;
		
		collection = this.getCookies();
		
		if ((null == collection) || collection.isEmpty()) 
			return Objects.toString(null);
		
		cookieFull = collection.get(0);
		
		delimiter = cookieFull.indexOf('=');
		
		return cookieFull.substring(delimiter + 1);
	}
	
	default List<String> getCookies (){
		return this.get("Cookie");
	}
	
	public String getFirst (final String key);
	
	public String getLast (final String key);
	
	public Set<String> keySet ();
	
	public int size ();
}