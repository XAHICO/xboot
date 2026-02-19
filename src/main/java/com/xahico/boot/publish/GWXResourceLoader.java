/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.publish;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class GWXResourceLoader {
	private static final Map<String, String> CACHE = new ConcurrentHashMap<>();
	private static boolean                   useCache = true;
	
	
	
	public static void configureUseCache (final boolean useCache){
		GWXResourceLoader.useCache = useCache;
	}
	
	public static String loadResource (final String resourceName){
		String resource;
		
		if (useCache) {
			resource = CACHE.get(resourceName);
			
			if (null != resource) {
				return resource;
			}
		}
		
		try (final var stream = GWXResourceLoader.class.getResourceAsStream(resourceName)) {
			resource = new String(stream.readAllBytes());
			
			CACHE.put(resourceName, resource);
			
			return resource;
		} catch (final IOException ex) {
			throw new InternalError(ex);
		}
	}
	
	
	
	private GWXResourceLoader (){
		throw new UnsupportedOperationException("Not supported.");
	}
}