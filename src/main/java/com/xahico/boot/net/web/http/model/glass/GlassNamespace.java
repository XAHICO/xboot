/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.net.web.http.model.glass;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class GlassNamespace implements GlassImportable {
	private final GlassNamespace      context;
	private final Map<String, String> variables = new HashMap<>();
	
	
	
	public GlassNamespace (){
		this(null);
	}
	
	public GlassNamespace (final GlassNamespace context){
		super();
		
		this.context = context;
	}
	
	
	
	@Override
	public String get (final String key){
		final String value;
		
		value = this.variables.get(key);
		
		if ((null == value) && (null != this.context)) {
			return this.context.get(key);
		}
		
		return value;
	}
	
	public void set (final String key, final String value){
		this.variables.put(key, value);
	}
	
	@Override
	public String toString (){
		final Iterator<String> it;
		final StringBuilder    sb;
		
		sb = new StringBuilder();
		
		if (null != this.context) {
			sb.append(this.context);
			sb.append(System.lineSeparator());
		}
		
		it = this.variables.keySet().iterator();
		
		while (it.hasNext()) {
			final String key;
			
			key = it.next();
			
			sb.append(key);
			sb.append(":");
			sb.append(" ");
			sb.append("\'");
			sb.append(this.variables.get(key));
			sb.append("\'");
			
			if (it.hasNext()) {
				sb.append(System.lineSeparator());
			}
		}
		
		return sb.toString();
	}
}