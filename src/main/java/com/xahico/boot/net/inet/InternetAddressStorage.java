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
public final class InternetAddressStorage implements Cloneable {
	public static final int AT_INET = 1;
	public static final int AT_INET6 = 2;
	public static final int AT_URL = 3;
	
	
	
	public static InternetAddressStorage getAddress (final Internet4Address address){
		return new InternetAddressStorage(AT_INET, address.getBytes());
	}
	
	public static InternetAddressStorage getAddress (final Internet6Address address){
		return new InternetAddressStorage(AT_INET6, address.getBytes());
	}
	
	public static InternetAddressStorage getAddress (final InternetAddress address) throws InvalidAddressException {
		if (address instanceof Internet4Address) 
			return getAddress((Internet4Address) address);
		else if (address instanceof Internet6Address) 
			return getAddress((Internet6Address) address);
		else if (address instanceof Url) 
			return getAddress((Url) address);
		else {
			throw new InvalidAddressException("");
		}
	}
	
	public static InternetAddressStorage getAddress (final Url address){
		return new InternetAddressStorage(AT_URL, address.getBytes());
	}
	
	public static InternetAddressStorage getLocalHost (){
		return getAddress(Internet4Address.getLocalHost());
	}
	
	public static InternetAddressStorage getLoopbackAddress(){
		return getAddress(Internet4Address.getLoopbackAddress());
	}
	
	
	
	private final byte[] data;
	private final int    type;
	
	
	
	private InternetAddressStorage (final int type, final byte[] data){
		super();
		
		this.type = type;
		this.data = data;
	}
	
	
	
	@Override
	public InternetAddressStorage clone (){
		try {
			return (InternetAddressStorage) super.clone();
		} catch (final CloneNotSupportedException ex) {
			throw new InternalError(ex);
		}
	}
	
	@Override
	public boolean equals (final Object obj){
		if (this == obj) 
			return true;
		
		if (null == obj || !(obj instanceof InternetAddressStorage)) 
			return false;
		else {
			final InternetAddressStorage other;
			
			other = (InternetAddressStorage) obj;
			
			return Objects.equals(this.getType(), other.getType()) 
				  && 
				 Arrays.equals(this.getBytes(), other.getBytes());
		}
	}
	
	public InternetAddress get (){
		try {
			switch (this.getType()) {
				case AT_INET:
					return Internet4Address.getByAddress(this.getBytes());
				case AT_INET6:
					return Internet6Address.getByAddress(this.getBytes());
				case AT_URL: 
					return Url.getByAddress(this.getBytes());
				default: {
					throw new InternalError("");
				}
			}
		} catch (final InvalidAddressException ex) {
			throw new InternalError(ex);
		}
	}
	
	public byte[] getBytes (){
		return this.data;
	}
	
	public int getType (){
		return this.type;
	}
	
	@Override
	public int hashCode (){
		int hash = 3;
		hash = 41 * hash + Arrays.hashCode(this.data);
		hash = 41 * hash + this.type;
		return hash;
	}
	
	public int length (){
		return this.data.length;
	}
	
	@Override
	public String toString (){
		switch (this.getType()) {
			case AT_INET:
				return this.get().toString();
			case AT_INET6:
				return this.get().toString();
			case AT_URL:
				return this.get().toString();
			default: {
				throw new InternalError("");
			}
		}
	}
}