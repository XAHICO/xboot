/*
 * Written by Tuomas Kontiainen <https://www.github.com/tutomiko>
 * 
 * Copyright (c) 2023, Tuomas Kontiainen
 */
package com.xahico.boot.net;

import com.xahico.boot.util.StringUtilities;
import java.net.URI;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * The {@code URIA} class is a more user-friendly abstraction of the 
 * Uniform Resource Identifier (URI).
 * 
 * URIA stands for Local Resource Identifier.
 * 
 * @author Tuomas Kontiainen
**/
public class URIA {
	private static final String QUERY_PARAMETER_DELIMITER = "=";
	private static final String QUERY_PARAMETER_SEPARATOR = "&";
	
	
	
	public static URIA create (final Object... parts){
		final StringBuilder sb;
		
		sb = new StringBuilder();
		
		for (final var part : parts) {
			final String string;
			
			string = part.toString();
			
			sb.append('/');
			
			for (var i = 0; i < string.length(); i++) {
				char c;
				
				c = string.charAt(i);
				
				if (c == '\\') 
					c = '/';
				
				if ((c == '/') && (!sb.isEmpty() && (sb.charAt(sb.length() - 1) == '/'))) 
					continue;
				
				sb.append(c);
			}
		}
		
		return URIA.create(sb.toString());
	}
	
	public static URIA create (final String string){
		return URIA.transform(URI.create(string));
	}
	
	public static URIA transform (final URI uri){
		return new URIA(uri);
	}
	
	
	
	private final URI uri;
	
	
	
	private URIA (final URI uri){
		super();
		
		this.uri = uri;
	}
	
	
	
	@Override
	public boolean equals (final Object obj){
		final URIA other;
		
		if (this == obj) 
			return true;
		
		if (null == obj) 
			return false;
		
		if (!(obj instanceof URIA)) 
			return false;
		
		other = (URIA)(obj);
		
		return Objects.equals(this.uri, other.uri);
	}
	
	public String getHost (){
		return this.uri.getHost();
	}
	
	public String getLocalizedPath (){
		if (this.uri.getPath().startsWith("/")) 
			return this.uri.getPath().substring(1);
		else {
			return this.uri.getPath();
		}
	}
	
	public String getPath (){
		return this.uri.getPath();
	}
	
	public String getPathFromWord (final int wordIndex){
		final Iterator<String> it;
		final StringBuilder    sb;
		
		sb = new StringBuilder();
		
		it = StringUtilities.splitStringIntoIterator(this.getPath(), "/", true);
		
		for (var skip = 0; it.hasNext(); skip++) {
			final String word;
			
			word = it.next();
			
			if (skip < wordIndex) {
				continue;
			}
			
			sb.append("/");
			sb.append(word);
		}
		
		return sb.toString();
	}
	
	public String getQuery (){
		return this.uri.getQuery();
	}
	
	public String getQueryParameter (final String lookupKey) throws NoSuchParameterException {
		final Iterator<String> it;
		final String           queryString;
		
		assert(null != lookupKey);
		
		queryString = this.uri.getQuery();
		
		if (null != queryString) {
			it = StringUtilities.splitStringIntoIterator(queryString, QUERY_PARAMETER_SEPARATOR, true);

			while (it.hasNext()) {
				final String key;
				final String queryParamString;
				final int    separator;
				final String value;

				queryParamString = it.next();

				separator = queryParamString.indexOf(QUERY_PARAMETER_DELIMITER);

				if (separator != -1) {
					key = queryParamString.substring(0, separator);

					if (key.equalsIgnoreCase(lookupKey)) {
						value = queryParamString.substring(separator + 1);

						return value;
					}
				}
			}
		}
		
		throw new NoSuchParameterException(String.format("No query parameter for key \'%s\'", lookupKey));
	}
	
	public List<URIAQueryParameter> getQueryParameters (){
		final String queryString;
		
		queryString = this.uri.getQuery();
		
		return StringUtilities.splitStringIntoObjectsList(queryString, QUERY_PARAMETER_SEPARATOR, true, (string) -> {
			final String key;
			final int    separator;
			final String value;
			
			separator = string.indexOf(QUERY_PARAMETER_DELIMITER);
			
			if (separator == -1) {
				key = value = "";
			} else {
				key = string.substring(0, separator);
				value = string.substring(separator + 1);
			}
			
			return new URIAQueryParameter(key, value);
		});
	}
	
	public String getRawPath (){
		return this.uri.getRawPath();
	}
	
	public String getRawQuery (){
		return this.uri.getRawQuery();
	}
	
	@Override
	public int hashCode (){
		int hash = 3;
		hash = 43 * hash + Objects.hashCode(this.uri);
		return hash;
	}
	
	public boolean isDefault (){
		return this.uri.getPath().equals("/");
	}
	
	@Override
	public String toString (){
		final StringBuilder sb;
		
		sb = new StringBuilder();
		sb.append(this.getPath());
		
		if (null != this.getQuery()) {
			sb.append("?");
			sb.append(this.getQuery());
		}
		
		return sb.toString();
	}
	
	public URI toURI (){
		return this.uri;
	}
}