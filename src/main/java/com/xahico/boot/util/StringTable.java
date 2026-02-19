/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.xahico.boot.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class StringTable  {
	public static final String PREFIX_REFERENCE = "$";
	public static final String PREFIX_SYMBOL_ANON = "Q";
	
	
	
	private final Map<String, String> references = new HashMap<>();
	
	
	
	public StringTable (){
		super();
	}
	
	
	
	public String createReference (final String value){
		final String referencePointer;
		
		referencePointer = createReferencePointer(value);
		
		references.putIfAbsent(referencePointer, value);
		
		return (PREFIX_REFERENCE + referencePointer);
	}
	
	private String createReferencePointer (final String value){
		return (PREFIX_SYMBOL_ANON + Integer.toHexString(Objects.toString(value).hashCode()));
	}
	
	public String getReference (String referencePointer){
		if (referencePointer.regionMatches(true, 0, PREFIX_REFERENCE, 0, PREFIX_REFERENCE.length())) 
			referencePointer = referencePointer.substring(PREFIX_REFERENCE.length());
		
		var variable = references.get(referencePointer);
		
		if (null == variable) 
			variable = references.get(referencePointer.toLowerCase());
		
		return variable;
	}
	
	public boolean isReference (final String s){
		if (! s.regionMatches(true, 0, PREFIX_REFERENCE, 0, PREFIX_REFERENCE.length())) 
			return false;
		else {
			return this.references.containsKey(s.substring(PREFIX_REFERENCE.length()));
		}
	}
	
	public String loadStrings (String string){
		int    stringBegin = -1;
		int    stringEnd;
		String stringFixed;
		String stringRaw;
		String stringRef;
		
		for (var i = 0; i < string.length(); i++) {
			char chr;
			
			chr = string.charAt(i);
			
			if (chr != '"') 
				continue;
			
			if (stringBegin == -1) 
				stringBegin = i;
			else {
				if ((i > 0) && string.charAt(i - 1) == '\\') {
					continue;
				}
				
				stringEnd = (i + 1);
				
				stringRaw = string.substring(stringBegin, stringEnd);
				
				stringFixed = stringRaw.substring(1, (stringRaw.length() - 1));
				
				stringFixed = stringFixed.replace("\\\"", "\"");
				
				stringRef = this.createReference(stringFixed);
				
				string = string.replace(stringRaw, stringRef);
				
				i = 0;
				
				stringBegin = -1;
			}
		}
		
		return string;
	}
	
	public int referenceCount (){
		return this.references().size();
	}
	
	public Map<String, String> references (){
		return this.references;
	}
}