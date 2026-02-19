/*
 * Written by Tuomas Kontiainen <https://www.github.com/tutomiko>
 * 
 * Copyright (c) 2023, Tuomas Kontiainen
 */
package com.xahico.boot.net.web.http;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public enum HttpMimeType {
	BINARY_STREAM("application/octet-stream"),
	CSS("text/css", ".css"),
	HTML("text/html", ".htm", ".html"),
	IMAGE_ICO("image/x-icon", ".ico"),
	IMAGE_JPEG("image/jpeg", ".jpg", ".jpeg"),
	JAVASCRIPT("text/javascript", ".js"),
	JSON("application/json", ".json"),
	NDJSON("application/x-x-ndjson"),
	SSE("text/event-stream"),
	TEXT("text/plain", ".txt");
	
	
	
	public static HttpMimeType parseHttpType (final String string){
		for (final var type : HttpMimeType.values()) {
			if (type.string.equalsIgnoreCase(string)) {
				return type;
			}
		}
		
		return HttpMimeType.TEXT;
	}
	
	
	
	private final String[] extensions;
	private final String   string;
	
	
	
	HttpMimeType (final String string, final String... extensions){
		this.string = string;
		this.extensions = extensions;
	}
	
	
	
	public String[] extensions (){
		return this.extensions;
	}
	
	public String string (){
		return this.string;
	}
}