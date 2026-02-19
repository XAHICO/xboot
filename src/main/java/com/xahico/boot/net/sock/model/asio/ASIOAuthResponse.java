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
public final class ASIOAuthResponse extends ASIOInstantObject {
	public byte[] cipherBytes;
	public int    options;
	
	
	
	@Override
	protected boolean decode (final byte[] array){
		final IOByteBuffer buffer;
		
		if (array.length < (Integer.BYTES + ASIOSecurityProvider.PACKET_SIZE)) 
			return false;
		
		buffer = IOByteBuffer.wrap(ByteBuffer.wrap(array));
		buffer.order(ByteOrder.BIG_ENDIAN);
		
		this.options = buffer.getInteger();
		
		this.cipherBytes = buffer.getBytes(ASIOSecurityProvider.PACKET_SIZE);
		
		return true;
	}
	
	@Override
	protected byte[] encode (){
		final IOByteBuffer buffer;
		
		buffer = new IOByteBuffer(Integer.BYTES + ASIOSecurityProvider.PACKET_SIZE);
		buffer.order(ByteOrder.BIG_ENDIAN);
		buffer.putInteger(this.options);
		
		if (null != this.cipherBytes) {
			buffer.putBytes(this.cipherBytes);
		}
		
		return buffer.array();
	}
}
