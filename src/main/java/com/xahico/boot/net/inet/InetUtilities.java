/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.net.inet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Arrays;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public class InetUtilities {
	public static byte[] getInet4Bytes (final String address) throws InvalidAddressException {
		final byte[] bytes;
		int          lastSeparator = -1;
		
		bytes = new byte[4];
		
		for (var i = 0; i < 3; i++) try {
			final int    ival;
			final int    separator;
			final String sval;
			
			separator = address.indexOf('.', (lastSeparator + 1));
			
			if (separator < 0) {
				throw new InvalidAddressException(String.format(""));
			}
			
			sval = address.substring((lastSeparator + 1), separator);
			
			if (sval.length() > 3) {
				throw new InvalidAddressException(String.format(""));
			}
			
			lastSeparator = separator;
			
			ival = Integer.parseInt(sval);
			
			if ((ival < 0) || (ival > 255)) {
				throw new InvalidAddressException(String.format(""));
			}
			
			bytes[i] = (byte)(ival);
		} catch (final NumberFormatException ex) {
			throw new InvalidAddressException(String.format(""));
		}
		
		return bytes;
	}
	
	public static long getInet4Long (final byte[] bytes){
		return ((bytes [0] & 0xFFl) << (3*8)) 
			   + 
			 ((bytes [1] & 0xFFl) << (2*8)) 
			   + 
			 ((bytes [2] & 0xFFl) << (1*8)) 
			   + 
			 (bytes [3] &  0xFFl); 
	}
	
	/**
	 * Returns an IPv4 -address that is guaranteed to not be looked up using 
	 * DNS-lookup.
	 * 
	 * @param addressBytes 
	 * TBD.
	 * 
	 * @return 
	 * TBD.
	 * 
	 * @throws InvalidAddressException 
	 * TBD.
	**/
	public static Inet4Address getInet4NoLookup (final byte[] addressBytes) throws InvalidAddressException {
		if (validateInet4(addressBytes)) try {
			return (Inet4Address) InetAddress.getByAddress(addressBytes);
		} catch (final UnknownHostException ex) {
			throw new Error(ex);
		} else {
			throw new InvalidAddressException("");
		}
	}
	
	public static Inet4Address getInet4NoLookup (final String address) throws InvalidAddressException {
		if (validateInet4(address)) try {
			return (Inet4Address) InetAddress.getByName(address);
		} catch (final UnknownHostException ex) {
			throw new Error(ex);
		} else {
			throw new InvalidAddressException(address);
		}
	}
	
	public static byte[] getInet6Bytes (final String address) throws InvalidAddressException {
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
	public static long getInetLong (final byte[] bytes) throws InvalidAddressException {
		if (bytes.length == 4) 
			return getInet4Long(bytes);
		else {
			throw new InvalidAddressException("");
		}
	}
	
	public static long getInetLong (final Inet4Address address){
		return getInet4Long(address.getAddress());
	}
	
	public static Inet4Address getPublicInet4 () throws IOException {
		try {
			final BufferedReader in;
			final String         ip;
			final URL            whatismyip;
			
			whatismyip = new URL("http://checkip.amazonaws.com");
			
			in = new BufferedReader(new InputStreamReader(whatismyip.openStream()));
			
			ip = in.readLine();
			
			return (Inet4Address) InetAddress.getByName(ip);
		} catch (final InvalidAddressException | MalformedURLException ex) {
			throw new InternalError(ex);
		}
	}
	
	public static Inet6Address getPublicInet6 () throws IOException {
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
	public static Inet4Address longToInet4 (final long iplong) throws InvalidAddressException {
		return getInet4NoLookup(longToInetBytes(iplong));
	}
	
	public static byte[] longToInetBytes (final long iplong){
		final byte b1;
		final byte b2;
		final byte b3;
		final byte b4;
		
		b4 = (byte)(iplong & 0xff);
		b3 = (byte)(iplong >> 8 & 0xff);
		b2 = (byte)(iplong >> 16 & 0xff);
		b1 = (byte)(iplong >> 24 & 0xff);
		
		return new byte[]{b1, b2, b3, b4};
	}
	
	public static boolean validateInet4 (final byte[] bytes){
		if (bytes.length != 4) 
			return false;
		
		for (var i = 0; i < bytes.length; i++) {
			final int b;
			
			b = Byte.toUnsignedInt(bytes[i]);
			
			if ((b < 0) || (b > 255)) {
				return false;
			}
		}
		
		return true;
	}
	
	public static boolean validateInet4 (final String string){
		int lastSeparator = -1;
		
		for (var i = 0; i < 4; i++) try {
			final int    ival;
			final int    separator;
			final String sval;
			
			if (i < 3) {
				separator = string.indexOf('.', (lastSeparator + 1));

				if (separator < 0) {
					return false;
				}

				sval = string.substring((lastSeparator + 1), separator);

				lastSeparator = separator;
			} else {
				sval = string.substring((lastSeparator + 1), string.length());
			}
			
			if (sval.length() > 3) {
				return false;
			}
			
			ival = Integer.parseInt(sval);
			
			if ((ival < 0) || (ival > 255)) {
				return false;
			}
		} catch (final NumberFormatException ex) {
			return false;
		}
		
		return true;
	}
	
	public static boolean validateInet6 (final byte[] bytes){
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
	public static boolean validateInet6 (final String address){
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
	
	
	private InetUtilities (){
		throw new UnsupportedOperationException("Not supported.");
	}
}