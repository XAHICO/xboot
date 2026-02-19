/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.reflection;

import java.lang.reflect.Field;

/**
 * TBD.
 * 
 * @param <T> 
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class ReflectionUtilities <T> {
	public static Object accessField (final Object instance, final String fieldName){
		try {
			final Field    field;
			final Class<?> instanceClass;
			
			instanceClass = instance.getClass();
			
			field = instanceClass.getDeclaredField(fieldName);
			field.setAccessible(true);
			
			return field.get(instance);
		} catch (final IllegalAccessException | NoSuchFieldException | SecurityException ex) {
			throw new Error(ex);
		}
	}
	
	public static void modifyField (final Object instance, final String fieldName, final Object newValue){
		try {
			final Field    field;
			final Class<?> instanceClass;
			
			instanceClass = instance.getClass();
			
			field = instanceClass.getDeclaredField(fieldName);
			field.setAccessible(true);
			field.set(instance, newValue);
		} catch (final IllegalAccessException | NoSuchFieldException | SecurityException ex) {
			throw new Error(ex);
		}
	}
	
	
	
	private ReflectionUtilities (){
		throw new UnsupportedOperationException("Not supported.");
	}
}