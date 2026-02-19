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
 * @author Tuomas Kontiainen
**/
public final class Domain extends InternetAddress implements CharSequence {
	public static Domain getByName (final String address) throws InvalidAddressException {
		return new Domain(address);
	}
	
	
	
	private final String address;
	
	
	
	private Domain (final String address){
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
		
		if (null == obj || !(obj instanceof Domain)) 
			return false;
		else {
			final Domain other;
			
			other = (Domain) obj;
			
			return Objects.equals(this.address, other.address);
		}
	}
	
	@Override
	public byte[] getBytes (){
		return this.address.getBytes();
	}
	
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