/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.io;

import java.io.IOException;
import java.nio.channels.SocketChannel;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public class IODynamicByteBuffer extends IOByteBuffer {
	private static final int DEFAULT_INITIAL_SIZE = 512;
	
	
	
	private final int initialCapacity;
	private int       preferredCapacity = 0;
	
	
	
	public IODynamicByteBuffer (){
		this(DEFAULT_INITIAL_SIZE);
	}
	
	public IODynamicByteBuffer (final int initialCapacity){
		super(initialCapacity);
		
		this.initialCapacity = initialCapacity;
	}
	
	@SuppressWarnings("OverridableMethodCallInConstructor")
	public IODynamicByteBuffer (final IOBuffer buffer){
		this(DEFAULT_INITIAL_SIZE);
		
		this.charset(buffer.charset());
	}
	
	
	
	public boolean canGrow (){
		return true;
	}
	
	@Override
	public IODynamicByteBuffer compact (){
		if (this.isFat() && this.isGreedy()) {
			this.shrink();
		}
		
		super.compact();
		
		return IODynamicByteBuffer.this;
	}
	
	private void ensureCapacity (final int increment){
		final int newCapacity;
		final int offeredCapacity;
		final int requiredCapacity;
		
		requiredCapacity = (this.length() + increment);
		
		if (requiredCapacity > this.capacity()) {
			if (! this.canGrow()) {
				// error handing ...
			}
			
			offeredCapacity = this.optimalCapacityWithIncrease(increment);
			
			if (requiredCapacity > offeredCapacity) 
				newCapacity = requiredCapacity;
			else {
				newCapacity = offeredCapacity;
			}
			
			this.reallocate(newCapacity, false);
		}
	}
	
	private void grow (){
		this.ensureCapacity(this.capacity() / 2);
	}
	
	public final int initialCapacity (){
		return this.initialCapacity;
	}
	
	public boolean isFat (){
		return (this.capacity() > (this.initialCapacity() * 2)); // Indicates bloating
	}
	
	public boolean isGreedy (){
		return (this.length() < (this.capacity() / 2));
	}
	
	public int optimalCapacityWithIncrease (final int increment){
		final int    blockSize;
		final double divisibleApprox;
		final double divisibleExact;
		final int    requiredCapacity;
		
		requiredCapacity = (this.length() + increment);
		
		if (requiredCapacity <= this.initialCapacity()) 
			return this.initialCapacity();
		
		blockSize = this.initialCapacity();
		
		divisibleExact = (((double)requiredCapacity) / ((double)blockSize));
		divisibleApprox = Math.round(divisibleExact);
		
		if (divisibleApprox < divisibleExact) 
			return (int)(1 + (divisibleExact * ((double)blockSize)));
		else {
			return (int)(1 + (divisibleApprox * ((double)blockSize)));
		}
	}
	
	@Override
	public IODynamicByteBuffer putByte (final int val){
		this.ensureCapacity(1);
		
		super.putByte(val);
		
		return IODynamicByteBuffer.this;
	}
	
	@Override
	public IODynamicByteBuffer putBytes (final byte[] val){
		this.ensureCapacity(val.length);
		
		super.putBytes(val);
		
		return IODynamicByteBuffer.this;
	}
	
	@Override
	public IOByteBuffer putBytes (final byte[] val, final int offset, final int count){
		this.ensureCapacity(val.length);
		
		super.putBytes(val, offset, count);
		
		return IODynamicByteBuffer.this;
	}
	
	public final int preferredCapacity (){
		if (this.preferredCapacity != 0) 
			return this.preferredCapacity;
		else {
			return this.initialCapacity();
		}
	}
	
	public final void preferredCapacity (final int capacity){
		this.preferredCapacity = capacity;
	}
	
	@Override
	public int read (final SocketChannel channel) throws IOException {
		if (this.isFull()) {
			if (! this.canGrow()) {
				// error handing ...
			}
			
			this.grow();
		}
		
		return super.read(channel);
	}
	
	public IODynamicByteBuffer shrink (){
		final int shrinkToSize;
		
		if (this.length() > this.preferredCapacity()) 
			shrinkToSize = this.length();
		else {
			shrinkToSize = this.preferredCapacity();
		}
		
		this.reallocate(shrinkToSize);
		
		return IODynamicByteBuffer.this;
	}
}