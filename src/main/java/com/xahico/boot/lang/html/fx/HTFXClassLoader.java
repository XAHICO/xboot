/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.lang.html.fx;

import com.xahico.boot.lang.html.HTMLException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public class HTFXClassLoader {
	private final String classPath;
	private final String directory;
	
	private final HTFXClassManifest manifest = new HTFXClassManifest();
	
	
	
	public HTFXClassLoader (final String directory, final String classPath){
		super();
		
		this.directory = directory;
		this.classPath = classPath;
	}
	
	
	
	public HTFXClass load () throws IOException {
		try {
			final boolean documentChanged;
			final boolean scriptChanged;
			final boolean styleChanged;
			
			documentChanged = this.loadBody();
			
			scriptChanged = this.loadScript();
			
			styleChanged = this.loadStylesheet();
			
			if (documentChanged || scriptChanged || styleChanged) {
				//System.out.println("component from disk (%s)".formatted(this.classPath));
			} else {
				//System.out.println("component from cache (%s)".formatted(this.classPath));
			}
		} catch (final HTMLException ex) {
			throw new Error(ex);
		}
		
		return new HTFXClass(this.classPath, this.manifest);
	}
	
	private boolean loadBody () throws HTMLException, IOException {
		final Path path;
		final long when;
		
		path = this.path(".html");
		
		when = Files.getLastModifiedTime(path).toMillis();
		
		if (when > manifest.documentTime) {
			manifest.document = Files.readString(path);
			
			manifest.documentTime = when;
			
			return true;
		}
		
		return false;
	}
	
	protected boolean loadScript () throws IOException {
		final Path path;
		final long when;
		
		path = this.path(".js");
		
		when = Files.getLastModifiedTime(path).toMillis();
		
		if (when > manifest.scriptTime) {
			manifest.script = Files.readString(path);
			
			manifest.scriptTime = when;
			
			return true;
		}
		
		return false;
	}
	
	protected boolean loadStylesheet () throws IOException {
		final Path path;
		final long when;
		
		path = this.path(".css");
		
		when = Files.getLastModifiedTime(path).toMillis();
		
		if (when > manifest.styleTime) {
			manifest.style = Files.readString(path);
			
			manifest.styleTime = when;
			
			return true;
		}
		
		return false;
	}
	
	private Path path (final String extension){
		return Paths.get(this.directory, (this.classPath.replace('.', '/') + extension));
	}
}