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
import static java.lang.System.currentTimeMillis;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Random;

/**
 * TBD.
 * 
 * @author tutomiko
**/
public class Internet4Address extends InternetAddress implements Comparable<Internet4Address> {
	public static final Internet4Address LOCALHOST = Internet4Address.getByName("127.0.0.1");
	
	
	
	public static Internet4Address currentPublic () throws IOException {
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
	// TODO: Requires validity check;
	public static Internet4Address getByAddress (final byte[] bytes) throws InvalidAddressException {
		return new Internet4Address(bytes);
	}
	
	public static Internet4Address getByName (final String addr) throws InvalidAddressException {
		if (! Internet4Address.validate(addr)) 
			throw new InvalidAddressException();
		else {
			final String[] bytes = addr.split("\\.+", 4);
			
			return new Internet4Address(
				Integer.valueOf(bytes[0]).byteValue(),
				Integer.valueOf(bytes[1]).byteValue(),
				Integer.valueOf(bytes[2]).byteValue(),
				Integer.valueOf(bytes[3]).byteValue() 
			);
		}
	}
	
	public static Internet4Address getLocalHost (){
		try {
			return Internet4Address.getByName("127.0.0.1");
		} catch (final InvalidAddressException ex) {
			throw new InternalError(ex);
		}
	}
	
	public static Internet4Address getLoopbackAddress (){
		try {
			return translateFromAddress(InetAddress.getLocalHost());
		} catch (final UnknownHostException ex) {
			throw new InternalError(ex);
		}
	}
	
	public static Internet4Address parseLong (final long addressLong){
		return Internet4Address.translateFromAddress(InetUtilities.longToInet4(addressLong));
	}
	
	public static Internet4Address random (){
		final int    b0, b1, b2, b3;
		final Random rand;
		
		rand = new Random(System.nanoTime() + Thread.currentThread().getId());
		
		b0 = rand.nextInt(255);
		b1 = rand.nextInt(255);
		b2 = rand.nextInt(255);
		b3 = rand.nextInt(255);
		
		return new Internet4Address( new byte[]{(byte)b0, (byte)b1, (byte)b2, (byte)b3} );
	}
	
	public static Internet4Address translateFromAddress (final InetAddress address){
		return new Internet4Address(address.getAddress());
	}
	
	public static boolean validate (final String addr){
		return InetUtilities.validateInet4(addr);
	}
	
	
	
	/*
	 * TBD.
	 */
	private final byte b1;
	private final byte b2;
	private final byte b3;
	private final byte b4;
	
	
	
	public Internet4Address (final byte b1, final byte b2, final byte b3, final byte b4){
		this.b1 = b1;
		this.b2 = b2;
		this.b3 = b3;
		this.b4 = b4;
	}
	
	public Internet4Address (final int b1, final int b2, final int b3, final int b4){
		this((byte) b1, (byte) b2, (byte) b3, (byte) b4);
	}
	
	public Internet4Address (final byte[] bytes){
		this(bytes[0], bytes[1], bytes[2], bytes[3]);
	}
	
	
	
	@Override
	public int compareTo (final Internet4Address other){
		return Long.compare(this.toLong(), other.toLong());
	}
	
	@Override
	public boolean equals (final Object obj){
		if (this == obj) 
			return true;
		
		if (null == obj || !(obj instanceof Internet4Address)) 
			return false;
		
		final var other = (Internet4Address) obj;
		
		return (this.b1 == other.b1) 
			  &&
			 (this.b2 == other.b2) 
			  &&
			 (this.b3 == other.b3) 
			  &&
			 (this.b4 == other.b4);
	}
	
	@Override
	public byte[] getBytes (){
		return new byte[]{b1, b2, b3, b4};
	}
	
	@Override
	public int hashCode (){
		int hash = 7;
		hash = 31 * hash + this.b1;
		hash = 31 * hash + this.b2;
		hash = 31 * hash + this.b3;
		hash = 31 * hash + this.b4;
		return hash;
	}
	
	public long toLong (){
		return InetUtilities.getInet4Long(this.getBytes());
	}
	
	@Override
	public String toString (){
		return String.format("%d.%d.%d.%d", 
			  Byte.toUnsignedInt(b1), 
			  Byte.toUnsignedInt(b2), 
			  Byte.toUnsignedInt(b3), 
			  Byte.toUnsignedInt(b4) 
		);
	}
}