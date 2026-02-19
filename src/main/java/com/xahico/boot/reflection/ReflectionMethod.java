/*
 * Written by Tuomas Kontiainen <https://www.github.com/tutomiko>
 * 
 * Copyright (c) 2023, Tuomas Kontiainen
 */
package com.xahico.boot.reflection;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.concurrent.ExecutionException;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class ReflectionMethod {
	private final Method       method;
	private final MethodHandle methodHandle;
	
	
	
	ReflectionMethod (final Method method, final MethodHandle methodHandle){
		super();
		
		this.method = method;
		this.methodHandle = methodHandle;
	}
	
	
	
	public boolean acceptsParameters (final Class<?>... classes){
		final Class[] parameterClasses;
		
		if (classes.length != this.getParameterCount()) 
			return false;
		
		parameterClasses = this.getParameterClasses();
		
		for (var i = 0; i < classes.length; i++) {
			if ((classes[i] != parameterClasses[i]) && !parameterClasses[i].isAssignableFrom(classes[i])) {
				return false;
			}
		}
		
		return true;
	}
	
	public <T extends Annotation> T getAnnotation (final Class<T> annotationClass){
		return this.method.getAnnotation(annotationClass);
	}
	
	public MethodHandle getHandle (){
		return this.methodHandle;
	}
	
	public String getName (){
		return this.method.getName();
	}
	
	public Class[] getParameterClasses (){
		final Class[]          classes;
		final ReflectionType[] types;

		types = this.getParameterTypes();

		classes = new Class[types.length];

		for (var i = 0; i < types.length; i++) {
			final Class          cclass;
			final ReflectionType ctype;

			ctype = types[i];

			cclass = ctype.getTypeClass();

			classes[i] = cclass;
		}

		return classes;
	}
	
	public int getParameterCount (){
		return this.method.getParameterCount();
	}
	
	public ReflectionType[] getParameterTypes (){
		final Type[]           ltTypeArray;
		final ReflectionType[] rtTypeArray;
		
		ltTypeArray = this.method.getGenericParameterTypes();
		
		rtTypeArray = new ReflectionType[ltTypeArray.length];
		
		for (var i = 0; i < ltTypeArray.length; i++) {
			rtTypeArray[i] = new ReflectionType(ltTypeArray[i]);
		}
		
		return rtTypeArray;
	}
	
	public Class<?> getReturnClass (){
		return this.getReturnType().getTypeClass();
	}
	
	public ReflectionType getReturnType (){
		return new ReflectionType(this.method.getReturnType());
	}
	
	public Object invoke (final Object... args) throws ExecutionException {
		try {
			return this.methodHandle.invokeWithArguments(args);
		} catch (final Throwable ex) {
			throw new ExecutionException(ex);
		}
	}
	
	public Object invokeDirect (final Object obj, final Object[] args) throws ExecutionException {
		try {
			return this.method.invoke(obj, args);
		} catch (final Throwable ex) {
			throw new ExecutionException(this.getName(), ex);
		}
	}
	
	public boolean isStatic (){
		return Modifier.isStatic(this.method.getModifiers());
	}
	
	@Override
	public String toString (){
		return this.method.toString();
	}
}