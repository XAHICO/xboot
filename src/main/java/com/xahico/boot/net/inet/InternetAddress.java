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

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * TBD.
 * 
 * @author tutomiko
**/
public abstract class InternetAddress implements Cloneable, Serializable {
	public static InternetAddress getByAddress (final byte[] bytes) throws InvalidAddressException {
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
	public static InternetAddress getByName (final String name) throws InvalidAddressException {
		if (name.equalsIgnoreCase("localhost") || name.equals("127.0.0.1")) {
			return Internet4Address.LOCALHOST;
		}
		
		if (name.equals("[::1]")) {
			return Internet6Address.LOCALHOST;
		}
		
		if (InetUtilities.validateInet4(name)) {
			return Internet4Address.getByName(name);
		}
		
		if (InetUtilities.validateInet6(name)) {
			return Internet6Address.getByName(name);
		}
		
		return Url.getByName(name);
	}
	
	
	
	InternetAddress (){
		super();
	}
	
	
	
	@Override
	@SuppressWarnings("CloneDeclaresCloneNotSupported")
	public InternetAddress clone (){
		try {
			return (InternetAddress) super.clone();
		} catch (final CloneNotSupportedException ex) {
			throw new InternalError(ex);
		}
	}
	
	@Override
	public boolean equals (final Object obj){
		if (this == obj) 
			return true;
		
		if (null == obj || !(obj instanceof InternetAddress)) 
			return false;
		else {
			final InternetAddress other;
			
			other = (InternetAddress) obj;
			
			return Arrays.equals(this.getBytes(), other.getBytes());
		}
	}
	
	public abstract byte[] getBytes ();
	
	@Override
	public int hashCode (){
		int hash = 3;
		hash = 41 * hash + Arrays.hashCode(this.getBytes());
		return hash;
	}
	
	@Override
	public String toString (){
		return new String(this.getBytes(), StandardCharsets.UTF_8);
	}
}