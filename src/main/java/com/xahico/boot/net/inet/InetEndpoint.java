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

import java.net.InetSocketAddress;
import java.util.Objects;

/**
 * TBD.
 * 
 * @author tutomiko
**/
public final class InetEndpoint implements Cloneable {
	public static InetEndpoint getByName (final String hostname) throws InvalidAddressException {
		final String address;
		final int    delimiter;
		final int    port;
		final String portName;
		
		delimiter = hostname.indexOf(':');
		
		if (delimiter == -1) 
			throw new InvalidAddressException("not a valid hostname: %s".formatted(hostname));
		
		address = hostname.substring(0, delimiter);
		portName = hostname.substring(delimiter + 1);
		
		try {
			port = Integer.parseInt(portName);
		} catch (final NumberFormatException ex) {
			throw new InvalidAddressException("not a valid hostname: %s; invalid port '%s'".formatted(hostname, portName));
		}
		
		return getEndpoint(address, port);
	}
	
	public static InetEndpoint getEndpoint (final InternetAddress address, final int port){
		return new InetEndpoint(address, port);
	}
	
	public static InetEndpoint getEndpoint (final String address, final int port){
		return getEndpoint(InternetAddress.getByName(address), port);
	}
	
	
	
	private final InternetAddress address;
	private final int             port;
	
	
	
	private InetEndpoint (final InternetAddress address, final int port){
		super();
		
		this.address = address;
		this.port = port;
	}
	
	
	
	@Override
	public InetEndpoint clone (){
		try {
			return (InetEndpoint) super.clone();
		} catch (final CloneNotSupportedException ex) {
			throw new InternalError(ex);
		}
	}
	
	@Override
	public boolean equals (final Object obj){
		if (this == obj) 
			return true;
		
		if (null == obj || !(obj instanceof InetEndpoint)) 
			return false;
		else {
			final InetEndpoint other;
			
			other = (InetEndpoint) obj;
			
			return Objects.equals(this.getPort(), other.getPort())
				  && 
				 Objects.equals(this.getAddress(), other.getAddress());
		}
	}
	
	public InternetAddress getAddress (){
		return this.address;
	}
	
	public String getAddressString (){
		return this.getAddress().toString();
	}
	
	public int getPort (){
		return this.port;
	}
	
	@Override
	public int hashCode (){
		int hash = 5;
		hash = 97 * hash + Objects.hashCode(this.address);
		hash = 97 * hash + Objects.hashCode(this.port);
		return hash;
	}
	
	public InetSocketAddress toSocketAddress (){
		return new InetSocketAddress(this.address.toString(), this.port);
	}
	
	@Override
	public String toString (){
		return (this.getAddress() + ":" + this.getPort());
	}
}