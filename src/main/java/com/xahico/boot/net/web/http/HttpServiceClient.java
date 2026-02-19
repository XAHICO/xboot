/*
 * Written by Tuomas Kontiainen <https://www.github.com/tutomiko>
 * 
 * Copyright (c) 2023, Tuomas Kontiainen
 */
package com.xahico.boot.net.web.http;

import com.sun.net.httpserver.HttpExchange;
import com.xahico.boot.util.StringUtilities;
import java.net.InetSocketAddress;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class HttpServiceClient {
	private static final String HEADER_XFF = "X-FORWARDED-FOR";
	private static final String LOOPBACK_LOCALHOST = "0:0:0:0:0:0:0:1";
	
	
	
	private final HttpExchange exchange;
	
	
	
	HttpServiceClient (final HttpExchange exchange){
		super();
		
		this.exchange = exchange;
	}
	
	
	
	/**
	 * Returns an IPv6 address describing the source of the connection.
	 * 
	 * @return 
	 * An IPv6 address describing the source of the connection.
	**/
	public String getAddress (){
		return this.getName().getHostString();
	}
	
	public String getAddressBehindProxy (){
		final String address;
		final String longString;
		final int    separator;
		
		longString = exchange.getRequestHeaders().getFirst(HEADER_XFF);
		
		if (null == longString) 
			return null;
		
		separator = longString.indexOf(',');
		
		if (separator == -1) 
			address = longString;
		else {
			address = longString.substring(0, separator);
		}
		
		return address.strip();
	}
	
	public List<String> getAddressProxies (){
		final String           address;
		final List<String>     collection;
		final Iterator<String> it;
		final String           longString;
		
		collection = new LinkedList<>();
		
		longString = exchange.getRequestHeaders().getFirst(HEADER_XFF);
		
		it = StringUtilities.splitStringIntoIterator(longString, ",", true);
		
		if (it.hasNext()) {
			address = it.next().strip();
			
			while (it.hasNext()) {
				final String proxy;
				
				proxy = it.next();
				
				collection.add(proxy.strip());
			}
		}
		
		return collection;
	}
	
	public String getDisplayString (){
		final String addressBehindProxy;
		
		addressBehindProxy = this.getAddressBehindProxy();
		
		if ((null != addressBehindProxy) && (!addressBehindProxy.isEmpty() && !addressBehindProxy.isBlank())) {
			return addressBehindProxy;
		}
		
		return this.getAddress();
	}
	
	public InetSocketAddress getName (){
		return exchange.getRemoteAddress();
	}
	
	public InetSocketAddress getNameBehindProxy (){
		return InetSocketAddress.createUnresolved(this.getAddressBehindProxy(), this.getPort());
	}
	
	public int getPort (){
		return this.getName().getPort();
	}
	
	public String getUniqueString (){
		return Integer.toHexString(Objects.hashCode(this.getUsableName()));
	}
	
	public String getUsableAddress (){
		if (this.isBehindProxy()) 
			return this.getAddressBehindProxy();
		else {
			return this.getAddress();
		}
	}
	
	public InetSocketAddress getUsableName (){
		if (this.isBehindProxy()) 
			return InetSocketAddress.createUnresolved(this.getAddressBehindProxy(), this.getPort());
		else {
			return InetSocketAddress.createUnresolved(this.getAddress(), this.getPort());
		}
	}
	
	public boolean isBehindProxy (){
		return exchange.getRequestHeaders().containsKey(HEADER_XFF);
	}
	
	public boolean isLocal (){
		return LOOPBACK_LOCALHOST.equals(this.getAddress());
	}
}