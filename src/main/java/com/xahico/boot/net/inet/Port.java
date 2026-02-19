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

import java.io.IOException;
import java.io.Serializable;
import java.net.ServerSocket;
import java.util.Random;

/**
 * TBD.
 * 
 * @author tutomiko
**/
public final class Port extends Number implements Cloneable, Comparable<Port>, Serializable {
	public static final int MAX_PORT = (Short.MAX_VALUE * 2) + 1;
	
	public static final int MAX_STRING = Integer.toString(MAX_PORT).length();
	
	
	/**
	 * TBD.
	**/
	public static final Port ADB_CLIENT_TCP_MAX = new Port(5585);
	
	/**
	 * TBD.
	**/
	public static final Port ADB_CLIENT_TCP_MIN = new Port(5555);
	
	/**
	 * Android Debug Bridge server, TCP/IP.
	**/
	public static final Port ADB_SERVER_TCP = new Port(5037);
	
	public static final Port DEFAULT_WEBHOST = new Port(80);
	public static final Port DEFAULT_WEBHOST2 = new Port(8080);
	
	/**
	 * TBD.
	**/
	public static final Port RANDOM = new Port(0);
	
	/**
	 * This port is commonly used for secure web browser communication.
	**/
	public static final Port SECURE_STANDARD = new Port(443);
	
	/**
	 * Server Message Block (NetBIOS).
	**/
	@Deprecated
	public static final Port SMB_NETBIOS = new Port(139);
	
	/**
	 * Server Message Block (supported on Windows 2000 and up), TCP/IP.
	**/
	public static final Port SMB_TCP = new Port(445);
	
	/**
	 * Telnet.
	**/
	public static final Port TELNET = new Port(23);
	
	
	
	public static boolean checkAvailable (final int port){
		if (port == 0) {
			return false;
		} try (final var __ = new ServerSocket(port)) {
			return true;
		} catch (final IOException ex) {
			return false;
		}
	}
	
	public static Port firstAvailable (final int min, final int max) throws UnavailableException {
		for (var pnum = min; pnum < max; pnum++) {
			if (checkAvailable(pnum)) {
				return Port.getPort(pnum);
			}
		}
		
		throw new UnavailableException(String.format("No available port in range %d-%d", min, max));
	}
	
	public static Port firstRandomAvailable () throws UnavailableException {
		try (final var socket = new ServerSocket(0)) {
			return Port.getPort(socket.getLocalPort());
		} catch (final IOException ex) {
			throw new UnavailableException("No available port");
		}
	}
	
	public static Port getPort (final int portnum) throws IllegalArgumentException {
		if (portnum > MAX_PORT) 
			throw new IllegalArgumentException("");
		else {
			return new Port(portnum);
		}
	}
	
	public static Port getPort (final short portnum){
		return new Port(Short.toUnsignedInt(portnum));
	}
	
	public static Port parsePort (final String portname){
		return getPort(Integer.parseInt(portname));
	}
	
	public static Port random (){
		return Port.random(1, MAX_PORT);
	}
	
	public static Port random (final int min, final int max){
		final int    pnum;
		final Random rand;
		
		rand = new Random();
		
		pnum = (rand.nextInt(max - min + 1) + min);
		
		return Port.getPort(pnum);
	}
	
	
	
	private final short portnum;
	
	
	
	private Port (final int portnum){
		this((short) portnum);
	}
	
	private Port (final short portnum){
		super();
		
		this.portnum = portnum;
	}
	
	
	
	@Override
	public Port clone (){
		try {
			return (Port) super.clone();
		} catch (final CloneNotSupportedException ex) {
			throw new InternalError(ex);
		}
	}
	
	@Override
	public int compareTo (final Port other){
		return Integer.compare(Port.this.intValue(), other.intValue());
	}
	
	@Override
	public double doubleValue (){
		return Port.this.intValue();
	}
	
	@Override
	public boolean equals (final Object obj){
		if (this == obj) 
			return true;
		
		if (null == obj) 
			return false;
		
		if (obj instanceof Integer) 
			return (((int) obj) == this.portnum);
		
		if (obj instanceof Short) 
			return (((short) obj) == this.portnum);
		
		if (!(obj instanceof Port)) 
			return false;
		else {
			final Port other;
			
			other = (Port) obj;
			
			return (this.portnum == other.portnum);
		}
	}
	
	@Override
	public float floatValue (){
		return Port.this.intValue();
	}
	
	@Override
	public int hashCode (){
		int hash = 5;
		hash = 59 * hash + this.portnum;
		return hash;
	}
	
	@Override
	public int intValue (){
		return Short.toUnsignedInt(Port.this.portnum);
	}
	
	public boolean isAvailable (){
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
	@Override
	public long longValue (){
		return Port.this.intValue();
	}
	
	public int num (){
		return this.portnum;
	}
	
	@Override
	public String toString (){
		return (Port.class.getSimpleName() + ": {" + Port.this.num() + "}");
	}
	
	public short value (){
		return (short)(Port.this.num());
	}
}