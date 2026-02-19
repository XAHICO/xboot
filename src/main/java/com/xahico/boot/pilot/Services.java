/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.pilot;

import java.util.HashMap;
import java.util.Map;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class Services {
	private static final Map<Class<?>, Service> instances = new HashMap<>();
	
	
	
	public static synchronized Service lookup (final Class<?> jclass) throws NoSuchServiceException {
		final Service service;
		
		service = instances.get(jclass);
		
		if (null != service) 
			return service;
		else {
			throw new NoSuchServiceException("service not found: %s".formatted(jclass));
		}
	}
	
	public static synchronized void register (final Service service){
		if (null == service) {
			throw new Error("");
		}
		
		instances.put(service.getInstanceClass(), service);
	}
	
	
	
	private Services (){
		throw new UnsupportedOperationException("Not supported.");
	}
}