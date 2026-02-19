/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.net.sock.model.asio;

import com.xahico.boot.reflection.Reflection;
import com.xahico.boot.reflection.ReflectionMethod;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class ASIOUtilities {
	public static byte[] addPadding (byte[] array){
		final int length;
		final int lengthPadded;
		
		length = array.length;
		lengthPadded = calculatePadding(array);
		
		if (lengthPadded != length) {
			final int padding;
			
			padding = (lengthPadded - length);
			
			array = Arrays.copyOf(array, lengthPadded);
			
			for (var i = 0; i < padding; i++) {
				array[length + i] = ASIOCryptor.ALGORITHM_PAD;
			}
		}
		
		return array;
	}
	
	public static int calculatePadding (final byte[] array){
		return calculatePadding(array.length);
	}
	
	public static int calculatePadding (final int length){
		if ((length % ASIOCryptor.ALGORITHM_BLOCK_LENGTH) != 0) {
			return (length + (ASIOCryptor.ALGORITHM_BLOCK_LENGTH - (length % ASIOCryptor.ALGORITHM_BLOCK_LENGTH)));
		} else {
			return length;
		}
	}
	
	static Set<ASIOMethod> getControlMethods (final Class<?> instanceClass){
		final Set<ASIOMethod> collection;
		final Reflection<?>   reflection;
		
		reflection = Reflection.of(instanceClass);
		
		collection = new HashSet<>();
		
		for (final ReflectionMethod reflectionMethod : reflection.getMethodsAnnotatedWith(ASIOControl.class, true)) {
			final ASIOControl annotation;
			final String      control;
			final int         expectParams;
			final ASIOMethod  exportMethod;
			final Class[]     paramClasses;
			final Class<?>    requestClass;
			final Class<?>    responseClass;
			
			if (reflectionMethod.isStatic())
				throw new Error(String.format("Invalid export method '%s' is static", reflectionMethod.getName()));
			
			annotation = reflectionMethod.getAnnotation(ASIOControl.class);
			
			control = annotation.value();
			
			expectParams = 2;
			paramClasses = reflectionMethod.getParameterClasses();
			
			if (paramClasses.length != expectParams) 
				throw new Error(String.format("Invalid declaration of method for '%s': invalid parameter count (expected %d, was %d)", reflectionMethod.getName(), expectParams, paramClasses.length));
			
			requestClass = paramClasses[0];
			
			responseClass = paramClasses[1];
			
			exportMethod = new ASIOMethod(reflectionMethod, control, requestClass, responseClass);
			
			collection.add(exportMethod);
		}
		
		return collection;
	}
	
	static Set<ASIOMethod> getHandlerMethods (final Class<?> instanceClass){
		final Set<ASIOMethod> collection;
		final Reflection<?>   reflection;
		
		reflection = Reflection.of(instanceClass);
		
		collection = new HashSet<>();
		
		for (final ReflectionMethod reflectionMethod : reflection.getMethodsAnnotatedWith(ASIOHandler.class, true)) {
			final ASIOHandler annotation;
			final String      control;
			final int         expectParams;
			final ASIOMethod  exportMethod;
			final Class[]     paramClasses;
			final Class<?>    requestClass;
			final Class<?>    responseClass;
			
			if (reflectionMethod.isStatic())
				throw new Error(String.format("Invalid export method '%s' is static", reflectionMethod.getName()));
			
			annotation = reflectionMethod.getAnnotation(ASIOHandler.class);
			
			control = annotation.value();
			
			expectParams = 2;
			paramClasses = reflectionMethod.getParameterClasses();
			
			if (paramClasses.length != expectParams) 
				throw new Error(String.format("Invalid declaration of method for '%s': invalid parameter count (expected %d, was %d)", reflectionMethod.getName(), expectParams, paramClasses.length));
			
			requestClass = paramClasses[0];
			
			responseClass = paramClasses[1];
			
			exportMethod = new ASIOMethod(reflectionMethod, control, requestClass, responseClass);
			
			collection.add(exportMethod);
		}
		
		return collection;
	}
	
	public static byte[] removePadding (final byte[] array){
		int length;
		
		length = array.length;
		
		for (var i = 0; i < array.length; i++) {
			if (array[i] == ASIOCryptor.ALGORITHM_PAD) {
				length = i;
				
				break;
			}
		}
		
		return Arrays.copyOfRange(array, 0, length);
	}
	
	
	
	private ASIOUtilities (){
		throw new UnsupportedOperationException("Not supported.");
	}
}