/*
 * Written by Tuomas Kontiainen <https://www.github.com/tutomiko>
 * 
 * Copyright (c) 2023, Tuomas Kontiainen
 */
package com.xahico.boot.net.inet;

import java.net.Inet4Address;
import java.net.InetAddress;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public class Internet4Pattern extends InetPattern {
	public static final Internet4Pattern PATTERN_ANY = getInet4Pattern("x.x.x.x");
	
	
	
	public static Internet4Pattern getInet4Pattern (final String pattern) throws InvalidPatternException {
		try {
			int         lastSeparator = -1;
			final int[] patternBytes;
			
			patternBytes = new int[4];
			
			for (var i = 0; i < patternBytes.length; i++) {
				final int    ival;
				final int    separator;
				final String sval;
				
				separator = pattern.indexOf('.', (lastSeparator + 1));
				
				if (i < (patternBytes.length - 1)) {
					if (separator < 0) {
						throw new InvalidPatternException(String.format(""));
					}

					sval = pattern.substring((lastSeparator + 1), separator);

					if (sval.length() > 3) {
						throw new InvalidPatternException(String.format(""));
					}

					lastSeparator = separator;
				} else {
					sval = pattern.substring((lastSeparator + 1), pattern.length());
				}

				if (sval.equalsIgnoreCase(TOKEN.repeat(sval.length()))) {
					patternBytes[i] = ANY_BYTE;
					
					continue;
				}

				ival = Integer.parseInt(sval);

				if ((ival < 0) || (ival > 255)) {
					throw new InvalidPatternException(String.format(""));
				}
				
				patternBytes[i] = ival;
			}
			
			return new Internet4Pattern(patternBytes[0], patternBytes[1], patternBytes[2], patternBytes[3]);
		} catch (final NumberFormatException ex) {
			throw new InvalidPatternException(String.format(""));
		}
	}
	
	public static boolean validateInet4Pattern (final String pattern){
		int lastSeparator = -1;
		
		for (var i = 0; i < 4; i++) try {
			final int    ival;
			final int    separator;
			final String sval;
			
			if (i < 3) {
				separator = pattern.indexOf('.', (lastSeparator + 1));

				if (separator < 0) {
					return false;
				}

				sval = pattern.substring((lastSeparator + 1), separator);
				
				lastSeparator = separator;
			} else {
				sval = pattern.substring((lastSeparator + 1), pattern.length());
			}
			
			if (sval.length() > 3) {
				return false;
			}
			
			if (sval.equalsIgnoreCase(TOKEN.repeat(sval.length()))) {
				continue;
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
	
	
	
	private final int b1;
	private final int b2;
	private final int b3;
	private final int b4;
	
	
	
	public Internet4Pattern (final int b1, final int b2, final int b3, final int b4){
		super();
		
		this.b1 = b1;
		this.b2 = b2;
		this.b3 = b3;
		this.b4 = b4;
	}
	
	
	
	@Override
	public String getPattern (){
		final StringBuilder sb;
		
		sb = new StringBuilder();
		
		if (this.b1 == ANY_BYTE) 
			sb.append(TOKEN_CHAR);
		else {
			sb.append((int)(this.b1));
		}
		
		sb.append('.');
		
		if (this.b2 == ANY_BYTE) 
			sb.append(TOKEN_CHAR);
		else {
			sb.append((int)(this.b2));
		}
		
		sb.append('.');
		
		if (this.b3 == ANY_BYTE) 
			sb.append(TOKEN_CHAR);
		else {
			sb.append((int)(this.b3));
		}
		
		sb.append('.');
		
		if (this.b4 == ANY_BYTE) 
			sb.append(TOKEN_CHAR);
		else {
			sb.append((int)(this.b4));
		}
		
		return sb.toString();
	}
	
	@Override
	public boolean matches (final byte[] addressBytes){
		if (addressBytes.length != 4) {
			System.out.println("NO MATCH: ");
			return false;
		}
		
		if ((this.b1 != ANY_BYTE) && (Byte.toUnsignedInt(addressBytes[0]) != this.b1)) {
			return false;
		}
		
		if ((this.b2 != ANY_BYTE) && (Byte.toUnsignedInt(addressBytes[1]) != this.b2)) {
			return false;
		}
		
		if ((this.b3 != ANY_BYTE) && (Byte.toUnsignedInt(addressBytes[2]) != this.b3)) {
			return false;
		}
		
		if ((this.b4 != ANY_BYTE) && (Byte.toUnsignedInt(addressBytes[3]) != this.b4)) {
			return false;
		}
		
		return true;
	}
	
	public boolean matches (final Inet4Address address){
		return this.matches(address.getAddress());
	}
	
	@Override
	public boolean matches (final InetAddress address){
		if (address instanceof Inet4Address) 
			return this.matches((Inet4Address)(address));
		else {
			return false;
		}
	}
}