/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.net.web.http.model.glass;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Simple Variadic Integration Markup (SVIM) language importer.
 * 
 * @author Tuomas Kontiainen
**/
public final class GlassImporter {
	private static final char SYM_REF = '$';
	private static final char SYM_REF_BEGIN = '(';
	private static final char SYM_REF_END = ')';
	
	
	
	public static String importString (final String string, final GlassNamespace importTable){
		final GlassImporter importer;
		
		try (final var stream = new ByteArrayOutputStream()) {
			importer = new GlassImporter();
			importer.setInput(new ByteArrayInputStream(string.getBytes()));
			importer.setOutput(stream);
			importer.setImportTable(importTable);
			importer.run();
			
			return stream.toString();
		} catch (final IOException ex) {
			throw new Error(ex);
		}
	}
	
	
	
	private GlassNamespace importTable = null;
	private InputStream    input = null;
	private OutputStream   output = null;
	
	
	
	public GlassImporter (){
		super();
	}
	
	
	
	private boolean pop () throws IOException {
		int                 b;
		char                c;
		final StringBuilder sb;
		
		sb = new StringBuilder();
		sb.append(SYM_REF);
		
		b = this.input.read();
		
		if (b == -1) {
			return false;
		}
		
		c = (char)(b);
		
		sb.append(c);
		
		if (c != SYM_REF_BEGIN) {
			this.output.write(sb.toString().getBytes());
			
			return true;
		}
		
		for (;;) {
			b = this.input.read();
			
			if (b == -1) {
				return false;
			}
			
			c = (char)(b);
			
			if (c == SYM_REF) {
				this.output.write(sb.toString().getBytes());
				
				return this.pop();
			}
			
			sb.append(c);
			
			if (c == SYM_REF_END) {
				final String referenceKey;
				final String referenceVal;
				
				referenceKey = sb.substring(2, (sb.length() - 1));
				referenceVal = this.importTable.get(referenceKey);
				
				this.output.write((null != referenceVal) ? referenceVal.getBytes() : "null".getBytes());
				
				break;
			}
		}
		
		return true;
	}
	
	public GlassImporter run () throws IOException {
		for (;;) {
			final int  b;
			final char c;
			
			b = this.input.read();
			
			if (b == -1) {
				break;
			}
			
			c = (char)(b);
			
			if (c == SYM_REF) {
				if (! this.pop()) {
					break;
				}
			} else {
				this.output.write(b);
			}
			
			this.output.flush();
		}
		
		return this;
	}
	
	public GlassImporter setImportTable (final GlassNamespace importTable){
		this.importTable = importTable;
		
		return this;
	}
	
	public GlassImporter setInput (final InputStream source){
		this.input = source;
		
		return this;
	}
	
	public GlassImporter setOutput (final OutputStream source){
		this.output = source;
		
		return this;
	}
}