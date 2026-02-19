/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.net.sock.model.bare;

import com.xahico.boot.reflection.Reflection;
import com.xahico.boot.reflection.ReflectionMethod;
import java.util.HashSet;
import java.util.Set;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class BAREUtilities {
	static Set<BAREMethod> getControlMethods (final Class<?> instanceClass){
		final Set<BAREMethod> collection;
		final Reflection<?>   reflection;
		
		reflection = Reflection.of(instanceClass);
		
		collection = new HashSet<>();
		
		for (final ReflectionMethod reflectionMethod : reflection.getMethodsAnnotatedWith(BAREControl.class, true)) {
			final BAREControl annotation;
			final String      control;
			final int         expectParams;
			final BAREMethod  exportMethod;
			final Class[]     paramClasses;
			final Class<?>    requestClass;
			final Class<?>    responseClass;
			
			if (reflectionMethod.isStatic())
				throw new Error(String.format("Invalid export method '%s' is static", reflectionMethod.getName()));
			
			annotation = reflectionMethod.getAnnotation(BAREControl.class);
			
			control = annotation.value();
			
			expectParams = 2;
			paramClasses = reflectionMethod.getParameterClasses();
			
			if (paramClasses.length != expectParams) 
				throw new Error(String.format("Invalid declaration of method for '%s': invalid parameter count (expected %d, was %d)", reflectionMethod.getName(), expectParams, paramClasses.length));
			
			requestClass = paramClasses[0];
			
			responseClass = paramClasses[1];
			
			exportMethod = new BAREMethod(reflectionMethod, control, requestClass, responseClass);
			
			collection.add(exportMethod);
		}
		
		return collection;
	}
	
	static Set<BARETransactionModel> loadTransactionModels (final Set<BAREMethod> methods){
		final Set<BARETransactionModel> collection;
		
		collection = new HashSet<>();
		
		for (final var method : methods) {
			final BARETransactionModel transactionModel;
			
			transactionModel = new BARETransactionModel(method.getControl(), method);
			
			collection.add(transactionModel);
		}
		
		return collection;
	}
	
	
	
	private BAREUtilities (){
		throw new UnsupportedOperationException("Not supported.");
	}
}