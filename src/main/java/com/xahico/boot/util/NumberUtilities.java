/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.ThreadLocalRandom;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class NumberUtilities {
	public static double buildDoubleFromBytes (final byte[] bytes, final ByteOrder order){
		final ByteBuffer buffer;
		
		buffer = ByteBuffer.allocate(Double.BYTES);
		buffer.order(order);
		buffer.put(bytes);
		buffer.rewind();
		
		return buffer.getDouble();
	}
	
	public static int buildIntegerFromBytes (final byte[] bytes, final ByteOrder order){
		final ByteBuffer buffer;
		
		buffer = ByteBuffer.allocate(Integer.BYTES);
		buffer.order(order);
		buffer.put(bytes);
		buffer.rewind();
		
		return buffer.getInt();
	}
	
	public static long buildLongFromBytes (final byte[] bytes, final ByteOrder order){
		final ByteBuffer buffer;
		
		buffer = ByteBuffer.allocate(Long.BYTES);
		buffer.order(order);
		buffer.put(bytes);
		buffer.rewind();
		
		return buffer.getLong();
	}
	
	public static boolean isBetweenRange (final int n, final int rangeMin, final int rangeMax){
		return ((n >= rangeMin) && (n <= rangeMax));
	}
	
	public static int lengthDifference (final long n, final long of){
		return (lengthOf(of) - lengthOf(n));
	}
	
	public static int lengthOf (final long n){
		if (n == 0) 
			return 1;
		else if (n == 10) 
			return 2;
		else if (n > 10) 
			return (int)(Math.round(Math.log10(n)) + 1);
		else if (n > 0) 
			return 1;
		else {
			return (lengthOf(Math.abs(n)) + 1); // was +1?
		}
	}
	
	public static int pseudoRandom (final int min, final int max){
		return ThreadLocalRandom.current().nextInt(min, (max + 1));
	}
	
	public static double translateDouble (final double n, final ByteOrder fromOrder){
		final ByteBuffer buffer;
		
		buffer = ByteBuffer.allocate(Double.BYTES);
		buffer.order(fromOrder);
		buffer.putDouble(0, n);
		buffer.rewind();
		
		return buffer.getDouble();
	}
	
	
	
	private NumberUtilities (){
		throw new UnsupportedOperationException("Not supported.");
	}
}