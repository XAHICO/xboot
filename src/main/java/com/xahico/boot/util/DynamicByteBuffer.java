/*
 * Written by Tuomas Kontiainen <https://www.github.com/tutomiko>
 * 
 * Copyright (c) 2023, Tuomas Kontiainen
 */
package com.xahico.boot.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public class DynamicByteBuffer {
	private int    cursor = 0;
	private byte[] xbuffer;
	
	
	
	public DynamicByteBuffer (){
		super();
		
		this.xbuffer = new byte[0];
	}
	
	public DynamicByteBuffer (final int initialSize){
		super();
		
		this.xbuffer = new byte[initialSize];
	}
	
	
	
	public byte[] array (){
		return xbuffer;
	}
	
	public int capacity (){
		return xbuffer.length;
	}
	
	public DynamicByteBuffer clear (){
		xbuffer = new byte[0];
		cursor = 0;
		
		return DynamicByteBuffer.this;
	}
	
	public DynamicByteBuffer expand (final int by){
		xbuffer = Arrays.copyOf(xbuffer, (this.capacity() + by));
		
		return DynamicByteBuffer.this;
	}
	
	public int length (){
		return xbuffer.length;
	}
	
	public DynamicByteBuffer put (final ByteBuffer buffer){
		return this.putBytes(buffer.array());
	}
	
	public DynamicByteBuffer putByte (final byte b){
		if (this.capacity() < (this.cursor + 1)) 
			this.expand(1);
		
		xbuffer[cursor] = b;
		
		cursor++;
		
		return DynamicByteBuffer.this;
	}
	
	public DynamicByteBuffer putByte (final int b){
		return this.putByte((byte)(b));
	}
	
	public DynamicByteBuffer putBytes (final byte[] bytes){
		if (this.capacity() < (this.cursor + bytes.length)) 
			this.expand((this.cursor + bytes.length) - this.capacity());
		
		for (var i = 0; i < bytes.length; i++) {
			final byte b;
			
			b = bytes[i];
			
			xbuffer[cursor] = b;
			
			cursor++;
		}
		
		return DynamicByteBuffer.this;
	}
	
	public DynamicByteBuffer putDouble (final double d){
		final ByteBuffer buffer;
		
		buffer = ByteBuffer.allocate(Double.BYTES);
		buffer.order(ByteOrder.BIG_ENDIAN);
		buffer.putDouble(0, d);
		buffer.rewind();
		
		return this.put(buffer);
	}
	
	public DynamicByteBuffer putInt (final int i){
		final ByteBuffer buffer;
		
		buffer = ByteBuffer.allocate(Integer.BYTES);
		buffer.order(ByteOrder.BIG_ENDIAN);
		buffer.putInt(0, i);
		buffer.rewind();
		
		return this.put(buffer);
	}
	
	public DynamicByteBuffer putLong (final long l){
		final ByteBuffer buffer;
		
		buffer = ByteBuffer.allocate(Long.BYTES);
		buffer.order(ByteOrder.BIG_ENDIAN);
		buffer.putLong(0, l);
		buffer.rewind();
		
		return this.put(buffer);
	}
}