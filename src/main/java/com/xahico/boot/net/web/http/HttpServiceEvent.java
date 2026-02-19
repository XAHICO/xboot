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
public final class HttpServiceEvent {
	private String data;
	private String type;
	
	
	
	public HttpServiceEvent (){
		super();
	}
	
	
	
	public String getData (){
		return this.data;
	}
	
	public String getType (){
		return this.type;
	}
	
	public byte[] pack (){
		final StringBuilder sb;
		final String        str;
		
		sb = new StringBuilder();
		sb.append("type:").append(this.type).append("\n");
		sb.append("data:").append(this.data).append("\n");
		sb.append("\n");
		
		str = sb.toString();
		
		return str.getBytes();
	}
	
	public void setData (final String data){
		this.data = data;
	}
	
	public void setType (final String type){
		this.type = type;
	}
}