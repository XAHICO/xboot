/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.distributed.identification;

import java.security.SecureRandom;

/**
 * TBD.
 * 
 * @author ChatGPT 5
**/
final class ULID {
	private static final char[]       BASE32 = "0123456789ABCDEFGHJKMNPQRSTVWXYZ".toCharArray();
	private static final SecureRandom RANDOM = new SecureRandom();
	
	
	
	public static String next (){
		long time = System.currentTimeMillis();
		
		byte[] randomness = new byte[10];
		
		RANDOM.nextBytes(randomness);
		
		char[] buffer = new char[26];
		
		// 48-bit timestamp → first 10 chars
		for (int i = 9; i >= 0; i--) {
			buffer[i] = BASE32[(int) (time & 31)];
			time >>>= 5;
		}

		// 80-bit randomness → next 16 chars
		int idx = 10;
		
		for (int i = 0; i < randomness.length; i++) {
			int v = randomness[i] & 0xFF;
			buffer[idx++] = BASE32[v >>> 3];
			buffer[idx++] = BASE32[v & 0x07];
		}
		
		return new String(buffer);
	}
	
	
	
	private ULID (){
		throw new UnsupportedOperationException("Not supported.");
	}
}