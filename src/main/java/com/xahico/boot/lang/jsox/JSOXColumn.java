/*
 * Written by Tuomas Kontiainen <https://www.github.com/tutomiko>
 * 
 * Copyright (c) 2023, Tuomas Kontiainen
 */
package com.xahico.boot.lang.jsox;

import com.xahico.boot.reflection.ReflectionField;
import com.xahico.boot.reflection.ReflectionType;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
final class JSOXColumn {
	private final ReflectionField handle;
	private final String          key;
	
	
	
	JSOXColumn (final ReflectionField handle, final String key){
		super();
		
		this.handle = handle;
		this.key = key;
	}
	
	
	
	public Class<?> actualType (){
		return this.handle.getType();
	}
	
	public ReflectionType genericType (){
		return this.handle.getGenericType();
	}
	
	public Object get (final JSOXObject instance){
		try {
			return this.handle.get(instance);
		} catch (final Throwable ex) {
			throw new InternalError(ex);
		}
	}
	
	public String key (){
		return this.key;
	}
	
	public String pattern (){
		final StringBuilder sb;
		
		sb = new StringBuilder();
		
		if (this.handle.isGenericType()) {
			final ReflectionType   generic;
			final ReflectionType[] generics;
			
			generic = this.handle.getGenericType();
			
			sb.append(JSOXUtilities.getJavaSyntaxString(generic.getTypeClass()));
			
			if (generic.isParameterized()) {
				sb.append("<");
				
				generics = generic.getGenericTypeArguments();
				
				for (var i = 0; i < generics.length; i++) {
//					sb.append(JSOXUtilities.getJavaSyntaxString(generics[i].getTypeClass()));
					sb.append(generics[i].toString());
					
					if ((i + 1) < generics.length) {
						sb.append(",");
						sb.append(" ");
					}
				}
				
				sb.append(">");
			}
		} else {
			sb.append(JSOXUtilities.getJavaSyntaxString(this.handle.getType()));
		}
		
		return sb.toString();
	}
	
	public void set (final JSOXObject instance, final Object newValue){
		try {
			this.handle.set(instance, newValue);
		} catch (final Throwable ex) {
			throw new InternalError(ex);
		}
	}
}