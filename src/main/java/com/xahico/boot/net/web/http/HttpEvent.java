/*
 * Written by Tuomas Kontiainen <https://www.github.com/tutomiko>
 * 
 * Copyright (c) 2023, Tuomas Kontiainen
 */
package com.xahico.boot.net.web.http;

import com.xahico.boot.event.Event;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class HttpEvent implements Event {
	private String data = null;
	private String event = null;
	
	
	
	public HttpEvent (){
		super();
	}
	
	
	
	public String getData (){
		return this.data;
	}
	
	public String getEvent (){
		return this.event;
	}
	
	@Override
	public String toString (){
		final StringBuilder sb;
		
		sb = new StringBuilder();
		sb.append("event: ").append(this.event);
		sb.append("\n");
		sb.append("data: ").append(this.data);
		sb.append("\n");
		sb.append("\n");
		
		return sb.toString();
	}
	
	public void setData (final String data){
		this.data = data;
	}
	
	public void setEvent (final String event){
		this.event = event;
	}
}