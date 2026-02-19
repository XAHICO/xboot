/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.lang.html.injection;

import com.xahico.boot.io.Source;
import com.xahico.boot.dev.Helper;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class HTMLInjectionContext {
	private final Map<String, Source> elements = new HashMap<>();
	private final List<Source>        scripts = new ArrayList<>();
	
	
	
	public HTMLInjectionContext (){
		super();
	}
	
	
	
	public HTMLInjectionContext addScript (final String script){
		this.addScriptInternal(Source.wrapString(script));
		
		return this;
	}
	
	@Helper
	public HTMLInjectionContext addScriptFile (final File file){
		return this.addScriptFile(file.getPath());
	}
	
	@Helper
	public HTMLInjectionContext addScriptFile (final Path filePath){
		return this.addScriptFile(filePath.toFile());
	}
	
	public HTMLInjectionContext addScriptFile (final String filePath){
		this.addScriptInternal(Source.wrapFile(filePath));
		
		return this;
	}
	
	private void addScriptInternal (final Source iSource){
		this.scripts.add(iSource);
	}
	
	public Map<String, Source> getElements (){
		return this.elements;
	}
	
	public List<Source> getScripts (){
		return this.scripts;
	}
	
	public HTMLInjectionContext clear (){
		this.elements.clear();
		this.scripts.clear();
		
		return this;
	}
	
	@Helper
	public HTMLInjectionContext setElementFile (final String key, final File file){
		return this.setElementFile(key, file.getPath());
	}
	
	@Helper
	public HTMLInjectionContext setElementFile (final String key, final Path filePath){
		return this.setElementFile(key, filePath.toFile());
	}
	
	public HTMLInjectionContext setElementFile (final String key, final String filePath){
		this.setElementInternal(key, Source.wrapFile(filePath));
		
		return this;
	}
	
	public HTMLInjectionContext setElementString (final String key, final String element){
		this.setElementInternal(key, Source.wrapString(element));
		
		return this;
	}
	
	private void setElementInternal (final String key, final Source iSource){
		this.elements.put(key, iSource);
	}
}