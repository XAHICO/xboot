/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.io;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class IOStringBuffer implements IOBuffer {
	private final IOByteBuffer  transitBuffer;
	private Charset             charset = DEFAULT_CHARSET;
	private final StringBuilder stringBuilder = new StringBuilder();
	
	
	
	public IOStringBuffer (){
		super();
		
		this.transitBuffer = new IODynamicByteBuffer().charset(this.charset());
	}
	
	public IOStringBuffer (final int initialSize){
		super();
		
		this.transitBuffer = new IODynamicByteBuffer(initialSize).charset(this.charset());
	}
	
	@SuppressWarnings("OverridableMethodCallInConstructor")
	public IOStringBuffer (final IOBuffer buffer){
		this();
		
		this.charset(buffer.charset());
	}
	
	
	
	@Override
	public boolean canRead (){
		return true;
	}
	
	@Override
	public boolean canWrite (){
		return true;
	}
	
	@Override
	public Charset charset (){
		return this.charset;
	}
	
	public IOStringBuffer charset (final Charset charset){
		this.charset = charset;
		this.transitBuffer.charset(charset);
		
		return IOStringBuffer.this;
	}
	
	@Override
	public IOStringBuffer clear (){
		this.discard(this.stringBuilder.length());
		
		return IOStringBuffer.this;
	}
	
	@Override
	public IOStringBuffer discard (final int count){
		this.stringBuilder.delete(0, count);
		
		return IOStringBuffer.this;
	}
	
	public int find (final char chr){
		return this.find(chr, 0);
	}
	
	public int find (final char chr, final int fromIndex){
		return this.find(Character.toString(chr), fromIndex);
	}
	
	@Override
	public int find (final int b, final int fromIndex){
		return this.find((char)(b), fromIndex);
	}
	
	public int find (final String string){
		return this.find(string, 0);
	}
	
	public int find (final String string, final int fromIndex){
		return this.stringBuilder.indexOf(string, fromIndex);
	}
	
	@Override
	public final char getChar (){
		return this.getChar(this.charset());
	}
	
	@Override
	public char getChar (final Charset charset){
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
	@Override
	public double getDouble (){
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
	@Override
	public int getInteger (){
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
	@Override
	public long getLong (){
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
	@Override
	public final String getString (){
		return this.toString(this.charset());
	}
	
	@Override
	public String getString (final Charset charset){
		return this.toString(charset);
	}
	
	@Override
	public boolean isFull (){
		return false;
	}
	
	@Override
	public int length (){
		return this.stringBuilder.length();
	}
	
	@Override
	public IOStringBuffer putByte (final int val){
		throw new UnsupportedOperationException("Not supported.");
	}
	
	@Override
	public IOStringBuffer putBytes (final byte[] val){
		return this.putBytes(val, 0, val.length);
	}
	
	@Override
	public IOStringBuffer putBytes (final byte[] val, final int offset, final int count){
		throw new UnsupportedOperationException("Not supported.");
	}
	
	@Override
	public IOStringBuffer putChar (final char chr){
		this.stringBuilder.append(chr);
		
		return IOStringBuffer.this;
	}
	
	@Override
	public IOStringBuffer putDouble (final double val){
		return this.putString(Double.toString(val));
	}
	
	@Override
	public IOStringBuffer putInteger (final int val){
		return this.putString(Integer.toString(val));
	}
	
	@Override
	public IOStringBuffer putLong (final long val){
		return this.putString(Long.toString(val));
	}
	
	@Override
	public final IOStringBuffer putString (final String string){
		this.stringBuilder.append(string);
		
		return IOStringBuffer.this;
	}
	
	@Override
	public final IOStringBuffer putString (final String string, final Charset charset){
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
	@Override
	public int read (final SocketChannel channel) throws IOException {
		final int bytesRead;
		
		this.transitBuffer.clear();
		
		bytesRead = this.transitBuffer.read(channel);
		
		this.putString(this.transitBuffer.toString());
		
		return bytesRead;
 	}
	
	@Override
	public String substring (final int offset, final int length, final Charset charset){
		return new String(this.stringBuilder.substring(offset, length).getBytes(charset));
	}
	
	@Override
	public byte[] toByteArray (final Charset charset){
		return this.stringBuilder.toString().getBytes(charset);
	}
	
	@Override
	public String toString (){
		return this.toString(this.charset());
	}
	
	@Override
	public String toString (final Charset charset){
		return new String(this.toByteArray(charset));
	}
	
	@Override
	public int write (final SocketChannel channel) throws IOException {
		final int bytesWritten;
		
		this.transitBuffer.clear();
		this.transitBuffer.putBytes(this.toByteArray());
		
		bytesWritten = this.transitBuffer.write(channel);
		
		return bytesWritten;
	}
}