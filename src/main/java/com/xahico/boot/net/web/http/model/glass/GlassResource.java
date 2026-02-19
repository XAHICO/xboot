/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.net.web.http.model.glass;

import com.xahico.boot.net.web.http.HttpMimeType;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class GlassResource {
	private boolean      attachment = false;
	private byte[]       content = null;
	private Path         contentPath = null;
	private HttpMimeType contentType = null;
	private String       name = null;
	
	
	
	GlassResource (){
		super();
	}
	
	
	
	public byte[] getContent (){
		if (null != this.content) 
			return this.content;
		
		if (null == this.contentPath) 
			return null;
		
		try {
			return Files.readAllBytes(this.contentPath);
		} catch (final IOException ex) {
			return null;
		}
	}
	
	public HttpMimeType getContentType (){
		return this.contentType;
	}
	
	public String getName (){
		return this.name;
	}
	
	public boolean hasContent (){
		return ((null != this.content) || (null != this.contentPath));
	}
	
	public boolean isAttachment (){
		return this.attachment;
	}
	
	public GlassResource setAttachment (final boolean attachment){
		this.attachment = attachment;
		
		return this;
	}
	
	public GlassResource setContent (final byte[] content){
		this.content = content;
		
		return this;
	}
	
	public GlassResource setContent (final String content){
		return this.setContent(content.getBytes());
	}
	
	public GlassResource setContent (final Path contentPath){
		this.contentPath = contentPath;
		
		return this;
	}
	
	public GlassResource setContentType (final HttpMimeType contentType){
		this.contentType = contentType;
		
		return this;
	}
	
	public GlassResource setName (final String name){
		this.name = name;
		
		return this;
	}
}