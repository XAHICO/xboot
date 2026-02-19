/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.net.sock.model.asio;

import com.xahico.boot.io.IOByteBuffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class ASIOAuthRequest extends ASIOInstantObject {
	public byte[] keyBytes;
	public int    keyLength;
	public int    options;
	public int    pingInterval;
	public int    pingTimeout;
	
	
	
	@Override
	protected boolean decode (final byte[] array){
		final IOByteBuffer buffer;
		
		if (array.length < (Integer.BYTES + Integer.BYTES + Integer.BYTES)) 
			return false;
		
		buffer = IOByteBuffer.wrap(ByteBuffer.wrap(array));
		buffer.order(ByteOrder.BIG_ENDIAN);
		
		this.options = buffer.getInteger();
		
		if ((this.options & ASIOOption.KEEP_ALIVE.flag()) != 0) {
			this.pingInterval = buffer.getInteger();
			this.pingTimeout = buffer.getInteger();
		}
		
		if ((this.options & ASIOOption.ENFORCE_TLS.flag()) != 0) {
			this.keyLength = buffer.getInteger();
			this.keyBytes = buffer.getBytes(this.keyLength);
		}
		
		return true;
	}
	
	@Override
	protected byte[] encode (){
		final IOByteBuffer buffer;
		int                packetSize;
		
		packetSize = 0;
		packetSize += Integer.BYTES; // OPTIONS
		
		if ((this.options & ASIOOption.KEEP_ALIVE.flag()) != 0) {
			packetSize += Integer.BYTES; // PING INTERVAL
			packetSize += Integer.BYTES; // PING TIMEOUT
		}
		
		if ((this.options & ASIOOption.ENFORCE_TLS.flag()) != 0) {
			packetSize += Integer.BYTES; // KEY LENGTH
			packetSize += this.keyBytes.length; // KEY BYTES
		}
		
		buffer = new IOByteBuffer(packetSize);
		buffer.order(ByteOrder.BIG_ENDIAN);
		buffer.putInteger(this.options);
		
		if ((this.options & ASIOOption.KEEP_ALIVE.flag()) != 0) {
			buffer.putInteger(this.pingInterval);
			buffer.putInteger(this.pingTimeout);
		}
		
		if ((this.options & ASIOOption.ENFORCE_TLS.flag()) != 0) {
			buffer.putInteger(this.keyLength);
			buffer.putBytes(this.keyBytes);
		}
		
		return buffer.array();
	}
}
