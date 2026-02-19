/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.reflection;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class ReflectionField {
	private final Field        field;
	private final MethodHandle methodHandleGet;
	private final MethodHandle methodHandleSet;
	
	
	
	ReflectionField (final Field field, final MethodHandle methodHandleGet, final MethodHandle methodHandleSet){
		super();
		
		this.field = field;
		this.methodHandleGet = methodHandleGet;
		this.methodHandleSet = methodHandleSet;
	}
	
	
	
	public Object get (final Object instance){
		try {
			return this.methodHandleGet.invoke(instance);
		} catch (final Throwable ex) {
			throw new InternalError(ex);
		}
	}
	
	public <T extends Annotation> T getAnnotation (final Class<T> jclass){
		return this.field.getAnnotation(jclass);
	}
	
	public ReflectionType getGenericType (){
		if (this.isGenericType()) 
			return new ReflectionType(this.field.getGenericType());
		else {
			return null;
		}
	}
	
	public String getName (){
		return this.field.getName();
	}
	
	public Class<?> getType (){
		return this.field.getType();
	}
	
	public boolean isAnnotationPresent (final Class<? extends Annotation> jclass){
		return this.field.isAnnotationPresent(jclass);
	}
	
	public boolean isGenericType (){
		return !this.field.getType().isPrimitive() && !this.field.getType().isArray();
	}
	
	public boolean isStatic (){
		return Modifier.isStatic(this.field.getModifiers());
	}
	
	public void set (final Object instance, final Object value){
		try {
			this.methodHandleSet.invoke(instance, value);
		} catch (final Throwable ex) {
			throw new InternalError(ex);
		}
	}
}