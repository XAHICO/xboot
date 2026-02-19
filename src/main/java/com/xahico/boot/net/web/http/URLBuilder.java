/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.net.web.http;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public class URLBuilder {
	private String       host = null;
	private String       path = null;
	private int          port = -1;
	private String       prefix = null;
	private HttpProtocol protocol = null;
	private String       query = null;
	
	
	
	public URLBuilder (){
		super();
	}
	
	
	
	public URLBuilder appendPath (final String path){
		if (null == this.path) {
			this.path = path;
		} else {
			this.path += "/";
			this.path += path;
		}
		
		return URLBuilder.this;
	}
	
	public URL build () throws MalformedURLException {
		return new URL(this.buildString());
	}
	
	public String buildString (){
		final StringBuilder sb;
		
		sb = new StringBuilder();
		
		if (null != this.protocol) {
			sb.append(this.protocol.name().toLowerCase());
			sb.append("://");
		}
		
		if (null != this.host) {
			if (null != this.prefix) {
				sb.append(this.prefix);
				
				if (! this.prefix.endsWith(".")) {
					sb.append(".");
				}
			}
			
			sb.append(this.host);
			
			if (this.port != -1) {
				sb.append(":");
				sb.append(this.port);
			}
		}
		
		if (null != this.path) {
			if (! this.path.startsWith("/")) {
				sb.append("/");
			}

			sb.append(this.path);

			if (null != this.query) {
				sb.append("?");
				sb.append(this.query);
			}
		}
		
		return sb.toString();
	}
	
	public String getHost (){
		return this.host;
	}
	
	public String getPath (){
		return this.path;
	}
	
	public int getPort (){
		return this.port;
	}
	
	public String getPrefix (){
		return this.prefix;
	}
	
	public HttpProtocol getProtocol (){
		return this.protocol;
	}
	
	public String getQuery (){
		return this.query;
	}
	
	public URLBuilder init (final URL url){
		this.host = url.getHost();
		this.path = url.getPath();
		this.port = url.getPort();
		this.protocol = HttpProtocol.valueOf(url.getProtocol().toUpperCase());
		this.query = url.getQuery();
		
		return URLBuilder.this;
	}
	
	public URLBuilder removeHost (){
		this.host = null;
		
		return URLBuilder.this;
	}
	
	public URLBuilder removePath (){
		this.path = null;
		
		return URLBuilder.this;
	}
	
	public URLBuilder removePort (){
		this.port = -1;
		
		return URLBuilder.this;
	}
	
	public URLBuilder removePrefix (){
		this.prefix = null;
		
		return URLBuilder.this;
	}
	
	public URLBuilder removeProtocol (){
		this.protocol = null;
		
		return URLBuilder.this;
	}
	
	public URLBuilder removeQuery (){
		this.query = null;
		
		return URLBuilder.this;
	}
	
	public URLBuilder setHost (final String host){
		this.host = host;
		
		return URLBuilder.this;
	}
	
	public URLBuilder setPath (final String path){
		this.path = path;
		
		return URLBuilder.this;
	}
	
	public URLBuilder setPort (final int port){
		this.port = port;
		
		return URLBuilder.this;
	}
	
	public URLBuilder setPrefix (final String prefix){
		this.prefix = prefix;
		
		return URLBuilder.this;
	}
	
	public URLBuilder setProtocol (final HttpProtocol protocol){
		this.protocol = protocol;
		
		return URLBuilder.this;
	}
	
	public URLBuilder setQuery (final String query){
		this.query = query;
		
		return URLBuilder.this;
	}
	
	@Override
	public String toString (){
		return this.buildString();
	}
}