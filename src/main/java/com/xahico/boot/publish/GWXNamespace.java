/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.publish;

import com.xahico.boot.lang.jsox.JSOXVariant;
import com.xahico.boot.reflection.Reflection;
import com.xahico.boot.synchronicity.ConcurrentFallbackMap;
import com.xahico.boot.util.transformer.ObjectTransformer;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class GWXNamespace {
	private final ConcurrentMap<String, Object> variables;
	
	
	
	GWXNamespace (){
		super();
		
		this.variables = new ConcurrentHashMap<>();
	}
	
	GWXNamespace (final GWXNamespace context){
		super();
		
		this.variables = new ConcurrentFallbackMap(context.variables);
	}
	
	
	
	public <T extends Enum> void addEnum (final Class<T> enumClass, final ObjectTransformer<T, String> transformer){
		this.addEnum(enumClass, ("enum:" + enumClass.getSimpleName()), transformer);
	}
	
	private <T extends Enum> void addEnum (final Class<T> enumClass, final String key, final ObjectTransformer<T, String> transformer){
		final Reflection<?> reflection;
		final JSOXVariant   struct;
		
		struct = new JSOXVariant();
		
		reflection = Reflection.of(enumClass);
		
		for (final var enumValue : reflection.getEnumValues()) {
			struct.put(enumValue.name(), transformer.call((T)enumValue));
		}
		
		this.set(key, struct.toJSONString());
	}
	
	public void clear (){
		this.variables.clear();
	}
	
	public String get (final String key){
		final Object value;
		
		value = this.variables.get(key);
		
		if (value instanceof String) {
			return ((String) value);
		} else {
			return Objects.toString(value);
		}
	}
	
	public void set (final String key, final boolean value){
		this.set(key, Boolean.toString(value));
	}
	
	public void set (final String key, final double value){
		this.set(key, Double.toString(value));
	}
	
	public void set (final String key, final int value){
		this.set(key, Integer.toString(value));
	}
	
	public void set (final String key, final long value){
		this.set(key, Long.toString(value));
	}
	
	public void set (final String key, final String value){
		this.variables.put(key, value);
	}
	
	@Override
	public String toString (){
		final Iterator<String> it;
		final StringBuilder    sb;
		
		sb = new StringBuilder();
		
		//if (null != this.context) {
		//	sb.append(this.context);
	//		sb.append(System.lineSeparator());
//		}
		
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
	
	public Map<String, Object> vars (){
		return this.variables;
	}
}