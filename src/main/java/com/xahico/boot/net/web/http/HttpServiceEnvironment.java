/*
 * Written by Tuomas Kontiainen <https://www.github.com/tutomiko>
 * 
 * Copyright (c) 2023, Tuomas Kontiainen
 */
package com.xahico.boot.net.web.http;

import com.xahico.boot.net.URIA;
import static com.xahico.boot.net.web.http.WebResourceManager.getWebResourceManager;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class HttpServiceEnvironment {
	private static final String PREFIX_INTERNAL_LOOKUP = "/~/";
	
	
	
	private File root = new File(".");
	
	
	
	HttpServiceEnvironment (){
		super();
	}
	
	
	
	public File lookup (final String path, final List<String> pathsIn) throws FileNotFoundException {
		if (path.startsWith(PREFIX_INTERNAL_LOOKUP)) 
			return lookupResourceInternal(path.substring(PREFIX_INTERNAL_LOOKUP.length()));
		
		try {
			return this.lookupElement(path, pathsIn);
		} catch (final FileNotFoundException ex) {
			return this.lookupResourceExternal(path);
		}
	}
	
	public File lookup (final URI uri, final List<String> pathsIn) throws FileNotFoundException {
		return this.lookup(uri.getPath(), pathsIn);
	}
	
	public File lookup (final URIA uri, final List<String> pathsIn) throws FileNotFoundException {
		return this.lookup(uri.getPath(), pathsIn);
	}
	
	private File lookupElement (final String path, final List<String> pathsIn) throws FileNotFoundException {
		File file;
		
		file = new File(this.root(), path);
		
		if (file.exists()) {
			return file;
		}
		
		if (null != pathsIn) {
			for (final var accessibleDirectory : pathsIn) {
				final URIA uri;

				uri = URIA.create(accessibleDirectory, path);

				file = new File(this.root(), uri.toString());

				if (file.exists()) {
					return file;
				}
			}
		}
		
		throw new FileNotFoundException(path);
	}
	
	private File lookupResourceExternal (final String path) throws FileNotFoundException {
		final File file;
		
		file = new File(this.root, path);
		
		if (! file.exists()) {
			throw new FileNotFoundException(path);
		}
		
		return file;
	}
	
	private File lookupResourceInternal (final String path) throws FileNotFoundException {
		if (path.startsWith("/") || path.startsWith("\\")) 
			return lookupResourceInternal(path.substring(1));
		else {
			return getWebResourceManager().getResourceFromPath(path);
		}
	}
	
	public File root (){
		return this.root;
	}
	
	public void root (final File root){
		this.root = root;
	}
}