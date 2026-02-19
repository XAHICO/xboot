/*
 * Written by Tuomas Kontiainen <https://www.github.com/tutomiko>
 * 
 * Copyright (c) 2023, Tuomas Kontiainen
 */
package com.xahico.boot.net.web.http;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class WebResourceManager {
	private static final String ROOT_CSS = "css";
	private static final String ROOT_HTML = "html";
	private static final String ROOT_JS = "js";
	private static final String ROOT_MISC = "misc";
	
	
	
	public static WebResourceManager getWebResourceManager (){
		synchronized (Singleton.INSTANCE) {
			return (Singleton.INSTANCE);
		}
	}
	
	public static String translateClassNameToResourceName (final Class<?> javaClass){
		return translateClassNameToResourceName(javaClass.getSimpleName());
	}
	
	public static String translateClassNameToResourceName (final String className){
		final StringBuilder sb;
		
		sb = new StringBuilder();
		
		for (var i = 0; i < className.length(); i++) {
			final char c;
			
			c = className.charAt(i);
			
			if (Character.isLowerCase(c)) 
				sb.append(c);
			else {
				if ((i > 0) && (!Character.isUpperCase(className.charAt(i - 1)) || Character.isLowerCase(className.charAt(i + 1)))) {
					sb.append('-');
				}
				
				sb.append(Character.toLowerCase(c));
			}
		}
		
		return sb.toString();
	}
	
	public static String translateResourcePathToInternal (final String resourcePath){
		final HttpServiceLookup lookup;
		final StringBuilder     sb;
		
		sb = new StringBuilder();
		
		lookup = HttpServiceLookup.lookup(resourcePath);
		
		if (lookup.hasRoot()) 
			sb.append(lookup.root());
		else {
			if (resourcePath.endsWith(".css")) 
				sb.append(ROOT_CSS);
			else if (resourcePath.endsWith(".js")) 
				sb.append(ROOT_JS);
			else if (resourcePath.endsWith(".html") || resourcePath.endsWith(".htm")) 
				sb.append(ROOT_HTML);
			else {
				sb.append(ROOT_MISC);
			}
		}
		
		sb.append('/');
		sb.append(lookup.item());
		
		return sb.toString();
	}
	
	
	
	private final Class<?> location = WebResourceManager.class;
	
	
	
	private WebResourceManager (){
		super();
	}
	
	
	
	public Class<?> getLocation (){
		return this.location;
	}
	
	public InputStream getResourceAsStreamFromPath (final URI resourcePath) throws FileNotFoundException {
		return getResourceAsStreamFromPath(resourcePath.getPath());
	}
	
	public InputStream getResourceAsStreamFromPath (final String resourcePath) throws FileNotFoundException {
		final InputStream resourceStream;
		
		resourceStream = this.getLocation().getResourceAsStream(translateResourcePathToInternal(resourcePath));
		
		if (null == resourceStream) 
			throw new FileNotFoundException(resourcePath);
		
		return resourceStream;
	}
	
	public File getResourceFromPath (final URI resourcePath) throws FileNotFoundException {
		return getResourceFromPath(resourcePath.getPath());
	}
	
	public File getResourceFromPath (final String resourcePath) throws FileNotFoundException {
		final URL resourceUrl;
		
		resourceUrl = this.getLocation().getResource(translateResourcePathToInternal(resourcePath));
		
		if (null == resourceUrl) 
			throw new FileNotFoundException(resourcePath);
		
		return new File(resourceUrl.getPath());
	}
	
	
	
	private static interface Singleton {
		WebResourceManager INSTANCE = new WebResourceManager();
	}
}