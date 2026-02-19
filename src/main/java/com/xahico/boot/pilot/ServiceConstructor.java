/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.pilot;

import com.xahico.boot.reflection.ReflectionMethod;
import java.util.concurrent.ExecutionException;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class ServiceConstructor {
	private final ReflectionMethod method;
	
	
	
	ServiceConstructor (final ReflectionMethod method){
		super();
		
		this.method = method;
	}
	
	
	
	public void invoke (final Object service, final ServiceProvider serviceProvider) throws ExecutionException {
		method.invoke(service, serviceProvider);
	}
}