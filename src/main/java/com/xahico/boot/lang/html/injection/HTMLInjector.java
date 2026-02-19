/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.lang.html.injection;

import com.xahico.boot.io.Source;
import com.xahico.boot.lang.html.HTMLDocument;
import com.xahico.boot.lang.html.HTMLException;
import com.xahico.boot.lang.html.HTMLNode;
import com.xahico.boot.lang.html.HTMLParser;
import com.xahico.boot.dev.Helper;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class HTMLInjector {
	private HTMLInjectionContext context = null;
	private OutputStream         destination = null;
	private Source               source = null;
	
	
	
	public HTMLInjector (){
		super();
	}
	
	
	
	private void ensureReady (){
		if (null == this.context) {
			throw new Error("context not set");
		}
		
		if (null == this.source) {
			throw new Error("not mounted");
		}
		
		if (null == this.destination) {
			throw new Error("no destination set");
		}
	}
	
	public HTMLDocument build () throws HTMLException, IOException {
		final HTMLDocument document;
		final HTMLParser   parser;
		
		this.ensureReady();
		
		parser = new HTMLParser();
		parser.setSource(this.source);
		
		document = parser.parse();
		
		return document;
	}
	
	@Helper
	public HTMLInjector mountFile (final File file){
		return this.mountFile(file.getPath());
	}
	
	@Helper
	public HTMLInjector mountFile (final Path filePath){
		return this.mountFile(filePath.toFile());
	}
	
	public HTMLInjector mountFile (final String filePath){
		this.source = Source.wrapFile(filePath);
		
		return this;
	}
	
	public HTMLInjector mountString (final String data){
		this.source = Source.wrapString(data);
		
		return this;
	}
	
	public HTMLInjector setContext (final HTMLInjectionContext context){
		this.context = context;
		
		return this;
	}
	
	public HTMLInjector setDestination (final OutputStream stream){
		this.destination = stream;
		
		return this;
	}
	
	public HTMLInjector setDestination (final StringBuilder sb){
		return this.setDestination(new OutputStream() {
			@Override
			public void write (final int b) throws IOException {
				final char c;
				
				c = (char)(b);
				
				sb.append(c);
			}
		});
	}
}