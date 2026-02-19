/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.publish;

import com.xahico.boot.lang.jsox.JSOXVariant;
import java.util.Map;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class GWXConnectionAdapter {
	public static GWXConnectionAdapter create (final String hostname, final Map<String, Object> configs){
		final GWXConnection connection;
		final int           delimiter;
		final String        hostString;
		final int           port;
		final String        portString;
		
		delimiter = hostname.lastIndexOf(':');
		
		if (delimiter == -1) {
			hostString = hostname;
			portString = null;
			
			port = 0;
		} else {
			hostString = hostname.substring(0, delimiter);
			portString = hostname.substring(delimiter + 1);
			
			port = Integer.parseInt(portString);
		}
		
		connection = GWXConnection.create(hostString, port, (config) -> {
			if (configs.containsKey("version")) {
				final Object value;
				final int    valueUsable;
				
				value = configs.get("version");
				
				if (null == value) 
					valueUsable = 0;
				else if (value instanceof Number) 
					valueUsable = (int)(value);
				else if (value instanceof String) 
					valueUsable = Integer.parseInt((String)value);
				else {
					throw new Error("invalid type for '%s': expected %s, got %s".formatted("version", Number.class, value.getClass()));
				}
				
				config.setRequestVersion(valueUsable);
			}
			
			if (configs.containsKey("tls")) {
				final Object  value;
				final boolean valueUsable;
				
				value = configs.get("tls");
				
				if (null == value) 
					valueUsable = false;
				else if (value instanceof Boolean) 
					valueUsable = ((boolean)value);
				else if (value instanceof Number) 
					valueUsable = (((int)value) == 1);
				else if (value instanceof String) 
					valueUsable = ((String)value).equalsIgnoreCase("true");
				else {
					throw new Error("invalid type for '%s': expected %s, got %s".formatted("tls", Boolean.class, value.getClass()));
				}
				
				config.setUseTLS(valueUsable);
			}
		});
		
		return new GWXConnectionAdapter(connection);
	}
	
	
	
	private final GWXConnection connection;
	
	
	
	GWXConnectionAdapter (final GWXConnection connection){
		super();
		
		this.connection = connection;
	}
	
	
	
	public void close (){
		this.connection.close();
	}
	
	public void set_token (final String token){
		this.connection.setToken(token);
	}
	
	public void transact_async (final String method, final String path, final Map<String, Object> data, final GWXTransactionCallbacks callbacks){
		this.transact_async(GWXPermission.parseSingleString(method), path, new JSOXVariant(data), callbacks);
	}
	
	private void transact_async (final GWXPermission method, final String path, final JSOXVariant data, final GWXTransactionCallbacks callbacks){
		this.transact_async_http(method.selectHttpMethod(), path, data, callbacks);
	}
	
	public void transact_async_http (final String method, final String path, final Map<String, Object> data, final GWXTransactionCallbacks callbacks){
		this.transact_async_http(method, path, new JSOXVariant(data), callbacks);
	}
	
	private void transact_async_http (final String method, final String path, final JSOXVariant data, final GWXTransactionCallbacks callbacks){
		this.connection.transactAsync(method, path, new GWXTransactionAdapter((_data_) -> _data_.assume(data), callbacks));
	}
}