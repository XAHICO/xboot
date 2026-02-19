/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.publish;

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
public final class GWXImporter {
	private static final char SYM_REF = '$';
	private static final char SYM_REF_BEGIN = '(';
	private static final char SYM_REF_END = ')';
	
	
	
	public static String importString (final String string, final GWXNamespace importTable){
		final GWXImporter importer;
		
		try (final var stream = new ByteArrayOutputStream()) {
			importer = new GWXImporter();
			importer.setInput(new ByteArrayInputStream(string.getBytes()));
			importer.setOutput(stream);
			importer.setImportTable(importTable);
			importer.run();
			
			return stream.toString();
		} catch (final IOException ex) {
			throw new Error(ex);
		}
	}
	
	
	
	private GWXNamespace importTable = null;
	private InputStream    input = null;
	private OutputStream   output = null;
	
	
	
	public GWXImporter (){
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
	
	public GWXImporter run () throws IOException {
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
	
	public GWXImporter setImportTable (final GWXNamespace importTable){
		this.importTable = importTable;
		
		return this;
	}
	
	public GWXImporter setInput (final InputStream source){
		this.input = source;
		
		return this;
	}
	
	public GWXImporter setOutput (final OutputStream source){
		this.output = source;
		
		return this;
	}
}