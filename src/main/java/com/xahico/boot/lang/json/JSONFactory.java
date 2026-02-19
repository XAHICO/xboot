/*
 * Written by Tuomas Kontiainen <https://www.github.com/tutomiko>
 * 
 * Copyright (c) 2023, Tuomas Kontiainen
 */
package com.xahico.boot.lang.json;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Constructor;
import org.json.JSONObject;

/**
 * ...
 * 
 * @param <T> 
 * ...
 * 
 * @author Tuomas Kontiainenn
**/
public final class JSONFactory <T extends JSONSerializable> {
	private static final Lookup PRIVILEGED_LOOKUP = MethodHandles.lookup();
	
	
	
	public static <T extends JSONSerializable> JSONFactory<T> getJSONFactory (final Class<T> jclass){
		try {
			final Constructor<T> defaultConstructor;
			final MethodHandle   defaultConstructorHandle;
			final Lookup         lookup;
			
			defaultConstructor = jclass.getConstructor();
			
			lookup = MethodHandles.privateLookupIn(jclass, PRIVILEGED_LOOKUP);
			
			defaultConstructorHandle = lookup.unreflectConstructor(defaultConstructor);
			
			return new JSONFactory<>(lookup, defaultConstructorHandle);
		} catch (final IllegalAccessException | NoSuchMethodException | SecurityException ex) {
			throw new Error(ex);
		}
	}
	
	
	
	private final MethodHandle defaultConstructorHandle;
	private final Lookup       lookup;
	
	
	
	private JSONFactory (final Lookup lookup, final MethodHandle defaultConstructorHandle){
		super();
		
		this.lookup = lookup;
		this.defaultConstructorHandle = defaultConstructorHandle;
	}
	
	
	
	public T newInstance (){
		try {
			final T instance;
			
			instance = (T) this.defaultConstructorHandle.invoke();
			
			return instance;
		} catch (final Throwable ex) {
			throw new Error(ex);
		}
	}
	
	public T newInstance (final JSONObject json){
		final T instance;
		
		instance = this.newInstance();
		instance.json(json);
		
		return instance;
	}
}