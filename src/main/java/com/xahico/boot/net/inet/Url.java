/*
 * Written by Tuomas Kontiainen <https://www.github.com/tutomiko>
 *
 * Copyright (C) 2022 Tuomas Kontiainen
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.xahico.boot.net.inet;

import java.util.Arrays;
import java.util.Objects;

/**
 * TBD.
 * 
 * @author tutomiko
**/
public final class Url extends InternetAddress implements CharSequence {
	public static Url getByAddress (final byte[] bytes) throws InvalidAddressException {
		for (var i = 0; i < bytes.length; i++) {
			final byte b;
			
			b = bytes[i];
			
			if (b == '\0') {
				return getByAddress(Arrays.copyOfRange(bytes, 0, i));
			}
		}
		
		return getByName( new String(bytes).strip() );
	}
	
	public static Url getByName (final String address) throws InvalidAddressException {
		return new Url(address);
	}
	
	
	
	private final String address;
	
	
	
	private Url (final String address){
		super();
		
		this.address = address;
	}
	
	
	
	@Override
	public char charAt (final int index){
		return address.charAt(index);
	}
	
	@Override
	public boolean equals (final Object obj){
		if (this == obj) 
			return true;
		
		if (null == obj || !(obj instanceof Url)) 
			return false;
		else {
			final Url other;
			
			other = (Url) obj;
			
			return Objects.equals(this.address, other.address);
		}
	}
	
	@Override
	public byte[] getBytes (){
		return this.address.getBytes();
	}
	
	/**
	 * Returns this url's domain name, consisting of the {@link #getName() name} and 
	 * {@link #getSuffix() suffix} (e.g. "google.com").
	 * 
	 * @return 
	 * This url's domain name or {@code null} if it is an invalid url.
	**/
	public String getDomain (){
		final String name;
		final String suffix;
		
		name = this.getName();
		
		if (null == name) 
			return null;
		
		suffix = this.getSuffix();
		
		if (null == suffix) 
			return null;
		
		return (name + '.' + suffix);
	}
	
	public String getFileName (){
		final String path;
		
		path = this.getPath();
		
		if (null == path) 
			return null;
		else {
			final int start;
			
			start = path.lastIndexOf('/');
			
			if (start == -1) 
				return path;
			else if (start + 1 >= path.length())
				return null;
			else {
				return path.substring(start + 1);
			}
		}
	}
	
	/**
	 * Returns this url's hostname, consisting of it's {@link #getPrefixes prefix(es)}, 
	 * {@link #getName name} and {@link #getSuffix suffix} 
	 * (e.g. "www.google.com").
	 * 
	 * @return 
	 * This url's hostname, consisting of any and all prefixes, the name and suffix.
	**/
	public String getHostname (){
		int end;
		int start;
		
		start = address.indexOf("://");
		
		if (start == -1) 
			start = 0;
		else {
			start += 3;
		}
		
		end = address.indexOf("/", start);
		
		if (end == -1) {
			end = address.length();
		}
		
		return address.substring(start, end);
	}
	
	/**
	 * Returns this url's {@link #getHostname domain name} without it's {@link #getSuffix suffix} 
	 * (e.g. "google" if the domain name is "www.google.com").
	 * 
	 * @return 
	 * This url's domain name without it's suffix, or {@code null} if the url is invalid.
	**/
	public String getName (){
		final String name;
		
		name = this.getHostname();
		
		if (null == name) 
			return null;
		else {
			final String[] segments;
			
			segments = name.split("\\.");
			
			if (null == segments || segments.length < 2) 
				return null;
			else if (segments.length == 2) 
				return segments[0];
			else {
				return segments[segments.length - 2];
			}
		}
	}
	
	/**
	 * Returns the path proceeding this url's {@link #getHostname hostname} 
	 * (e.g. "www.google.com").
	 * 
	 * @return 
	 * The path proceeding this url's hostname, or {@code null} if no path is present.
	**/
	public String getPath (){
		int start;
		
		start = address.indexOf("://");
		
		if (start == -1) 
			start = 0;
		else {
			start += 3;
		}
		
		start = address.indexOf("/", start);
		
		if (start == -1) 
			return null;
		else {
			start++;
		}
		
		return address.substring(start, address.length());
	}
	
	/**
	 * Returns this url's first {@link #getPrefixes prefix} (e.g. "www.", without the dot).
	 * 
	 * @return 
	 * This url's first prefix, or {@code null} if no prefix is present.
	**/
	public String getPrefix (){
		final String name;
		
		name = this.getHostname();
		
		if (null == name) 
			return null;
		else {
			final String[] segments;
			
			segments = name.split("\\.");
			
			if (null == segments || segments.length < 3) 
				return null;
			else {
				return segments[0];
			}
		}
	}
	
	/**
	 * Returns an array of prefixes preceeding this url's {@link #getDomain domain}.
	 * 
	 * @return 
	 * An array of prefixes preceeding this url's domain.
	**/
	public String[] getPrefixes (){
		final String name;
		
		name = this.getHostname();
		
		if (null == name) 
			return new String[0];
		else {
			final String[] segments;
			
			segments = name.split("\\.");
			
			if (null == segments || segments.length < 2) 
				return new String[0];
			else {
				return Arrays.copyOfRange(segments, 0, segments.length - 2);
			}
		}
	}
	
	/**
	 * Returns this url's protocol (e.g. "http://", without the '://').
	 * 
	 * @return 
	 * This url's protocol, or {@code null} if no protocol is specified.
	**/
	public String getProtocol (){
		final int end;
		
		end = address.indexOf("://");
		
		if (end == -1) 
			return null;
		else {
			return address.substring(0, end);
		}
	}
	
	/**
	 * Returns this url's suffix (e.g. ".com", without the dot).
	 * 
	 * @return 
	 * This url's suffix, or {@code null} if no suffix is present, in which event the url is 
	 * invalid.
	**/
	public String getSuffix (){
		final String name;
		
		name = this.getHostname();
		
		if (null == name) 
			return null;
		else {
			final String[] segments;
			
			segments = name.split("\\.");
			
			if (null == segments || segments.length < 2) 
				return null;
			else {
				return segments[segments.length - 1];
			}
		}
	}
	
	@Override
	public int hashCode (){
		int hash = 3;
		hash = 97 * hash + Objects.hashCode(this.address);
		return hash;
	}
	
	public boolean isDirPath (){
		final String path;
		
		path = this.getPath();
		
		if (null == path) 
			return false;
		else {
			return path.endsWith("/");
		}
	}
	
	/**
	 * Returns this url's length.
	 * 
	 * @return 
	 * This url's length.
	**/
	@Override
	public int length (){
		return this.address.length();
	}
	
	@Override
	public CharSequence subSequence (final int start, final int end){
		return this.address.subSequence(start, end);
	}
	
	@Override
	public String toString (){
		return this.address;
	}
}