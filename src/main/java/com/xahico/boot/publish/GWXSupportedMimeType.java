/*
 * Written by Tuomas Kontiainen <https://www.github.com/tutomiko>
 * 
 * Copyright (c) 2023, Tuomas Kontiainen
 */
package com.xahico.boot.publish;

import com.xahico.boot.util.ArrayUtilities;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public enum GWXSupportedMimeType {
	BINARY_STREAM("application/octet-stream", ".bin", ".exe", ".dll", ".sys" , ".so", ".zip", ".db"),
	CSS("text/css", ".css"),
	HTML("text/html", ".htm", ".html"),
	IMAGE_ICO("image/x-icon", ".ico"),
	IMAGE_JPEG("image/jpeg", ".jpg", ".jpeg"),
	IMAGE_PNG("image/png", ".png"),
	JAVASCRIPT("text/javascript", ".js"),
	JSON("application/json", ".json"),
	NDJSON("application/x-x-ndjson"),
	SSE("text/event-stream"),
	TEXT("text/plain", ".txt");
	
	
	
	public static GWXSupportedMimeType getMimeTypeForExtension (final String extension, final GWXSupportedMimeType defaultTo){
		if (null == extension) 
			return defaultTo;
		
		for (final var type : GWXSupportedMimeType.values()) {
			if (ArrayUtilities.containsStringIgnoreCase(type.extensions, extension)) {
				return type;
			}
		}
		
		return defaultTo;
	}
	
	public static GWXSupportedMimeType parseMimeType (final String string, final GWXSupportedMimeType defaultTo){
		for (final var type : GWXSupportedMimeType.values()) {
			if (type.string.equalsIgnoreCase(string)) {
				return type;
			}
		}
		
		return defaultTo;
	}
	
	
	
	private final String[] extensions;
	private final String   string;
	
	
	
	GWXSupportedMimeType (final String string, final String... extensions){
		this.string = string;
		this.extensions = extensions;
	}
	
	
	
	public String[] extensions (){
		return this.extensions;
	}
	
	public boolean isImage (){
		return this.string.startsWith("image/");
	}
	
	@Override
	public String toString (){
		return this.string;
	}
}