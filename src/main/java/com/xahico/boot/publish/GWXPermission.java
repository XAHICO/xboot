/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.xahico.boot.publish;

import com.xahico.boot.util.ArrayUtilities;
import com.xahico.boot.util.StringUtilities;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public enum GWXPermission {
	CREATE('c', "POST"),
	READ('r', "GET"),
	UPDATE('u', "PATCH", "PUT"),
	DELETE('d', "DELETE"),
	OBSERVE('O');
	
	
	
	public static GWXPermission forHttpMethod (final String method){
		if (null == method) 
			return null;
		
		for (final var accessMode : GWXPermission.values()) {
			if (ArrayUtilities.containsStringIgnoreCase(accessMode.httpMethods, method)) {
				return accessMode;
			}
		}
		
		return null;
	}
	
	public static Set<GWXPermission> parseMultiString (final String string){
		final Set<GWXPermission> collection;
		
		collection = new HashSet<>();
		
		for (var i = 0; i < string.length(); i++) {
			final char c;
			
			c = string.charAt(i);
			
			for (final var accessMode : GWXPermission.values()) {
				if (Character.toLowerCase(c) == Character.toLowerCase(accessMode.key)) {
					collection.add(accessMode);
				}
			}
		}
		
		return collection;
	}
	
	public static GWXPermission[] parsePathString (final String string){
		final List<GWXPermission> accessPath;
		final Iterator<String>            it;
		
		if (string.isBlank()) {
			return new GWXPermission[]{null};
		}
		
		accessPath = new LinkedList<>();
		
		it = StringUtilities.splitStringIntoIterator(string, "/", true);
		
		while (it.hasNext()) {
			final String word;
			
			word = it.next();
			
			if (word.equals("?")) {
				accessPath.add(null);
				
				continue;
			}
			
			accessPath.add(GWXPermission.parseSingleString(word));
		}
		
		return accessPath.toArray(new GWXPermission[0]);
	}
	
	public static GWXPermission parseSingleString (final char c){
		for (final var accessMode : GWXPermission.values()) {
			if (Character.toLowerCase(c) == Character.toLowerCase(accessMode.key)) {
				return accessMode;
			}
		}
		
		return null;
	}
	
	public static GWXPermission parseSingleString (final String string){
		if (string.strip().length() == 1) 
			return parseSingleString(string.charAt(0));
		
		for (final var accessMode : GWXPermission.values()) {
			if (accessMode.name().equalsIgnoreCase(string.strip())) {
				return accessMode;
			}
		}
		
		return null;
	}
	
	public static GWXPermission transformHttpMethod (final String httpMethod){
		for (final var accessMode : GWXPermission.values()) {
			if (ArrayUtilities.containsStringIgnoreCase(accessMode.httpMethods, httpMethod)) {
				return accessMode;
			}
		}
		
		return null;
	}
	
	
	
	private final String[] httpMethods;
	private final char     key;
	
	
	
	GWXPermission (final char key, final String... httpMethods){
		this.key = key;
		this.httpMethods = httpMethods;
	}
	
	
	
	public String[] httpMethods (){
		return this.httpMethods;
	}
	
	public char key (){
		return this.key;
	}
	
	public String selectHttpMethod (){
		if (this.httpMethods.length == 0) 
			return null;
		else {
			return this.httpMethods[0];
		}
	}
}