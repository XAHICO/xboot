/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.io;

import com.xahico.boot.dev.Helper;
import com.xahico.boot.util.Exceptions;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.SocketChannel;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public class IOSocketChannel implements AutoCloseable {
	private static final int DEFAULT_BUFFER_SIZE = 512;
	
	
	
	public static IOSocketChannel open () throws IOException {
		return IOSocketChannel.wrap(SocketChannel.open());
	}
	
	public static IOSocketChannel wrap (final SocketChannel channel){
		return new IOSocketChannel(channel);
	}
	
	
	
	private int                 bufferSize = DEFAULT_BUFFER_SIZE;
	private final SocketChannel channel;
	
	
	
	protected IOSocketChannel (final SocketChannel channel){
		super();
		
		this.channel = channel;
	}
	
	
	
	@Override
	public void close (){
		try {
			this.channel.close();
		} catch (final IOException ex) {
			Exceptions.ignore(ex);
		}
	}
	
	private IOByteBuffer createDefaultBuffer (){
		return new IOByteBuffer(this.bufferSize);
	}
	
	private IOByteBuffer createDefaultBuffer (final int requiredCapacity){
		final int useCapacity;
		
		if (requiredCapacity < this.bufferSize) 
			useCapacity = requiredCapacity;
		else {
			useCapacity = this.bufferSize;
		}
		
		return new IOByteBuffer(useCapacity);
	}
	
	public long read (final OutputStream stream, final IOByteBuffer medium) throws IOException {
		int  bytesRead;
		long bytesReadTotal = 0;
		
		do {
			medium.clear();
			medium.rewind();
			
			try {
				bytesRead = medium.read(this.channel);
			} catch (final IOException ex) {
				break;
			}
			
			if (bytesRead > 0) {
				bytesReadTotal += bytesRead;
				
				stream.write(medium.array(), 0, bytesRead);
			}
		} while (true);
		
		return bytesReadTotal;
	}
	
	public long read (final IOBuffer buffer) throws IOException {
		return buffer.read(this.channel);
	}
	
	public void setBufferSize (final int bufferSize){
		this.bufferSize = bufferSize;
	}
	
	public final SocketChannel wrappedChannel (){
		return this.channel;
	}
	
	@Helper
	public long write (final byte[] buffer) throws IOException {
		return this.write(buffer, buffer.length, this.createDefaultBuffer(buffer.length));
	}
	
	public long write (final byte[] buffer, final int count, final IOByteBuffer medium) throws IOException {
		int blockSize;
		int cursor = 0;
		int remaining = count;
		int send;
		
		while (remaining > 0) {
			blockSize = (remaining >= medium.capacity() ? medium.capacity() : remaining);
			
			medium.clear();
			medium.rewind();
			medium.putBytes(buffer, cursor, blockSize);
			
			send = medium.write(this.channel);
			
			remaining -= send;
			cursor += send;
		}
		
		return buffer.length;
	}
	
	@Helper
	public long write (final byte[] buffer, final IOByteBuffer medium) throws IOException {
		return this.write(buffer, buffer.length, medium);
	}
	
	@Helper
	public long write (final InputStream stream) throws IOException {
		return this.write(stream, this.createDefaultBuffer());
	}
	
	public long write (final InputStream stream, final long count, final IOByteBuffer medium) throws IOException {
		int          blockSize;
		final byte[] buffer;
		long         remaining = count;
		
		buffer = new byte[this.bufferSize];
		
		while (remaining > 0) {
			blockSize = stream.read(buffer);
			
			if (blockSize == -1) {
				throw new IOException("stream EOF encountered at %d (was supposed to reach at least %d)".formatted((count - remaining), count));
			}
			
			remaining -= this.write(buffer, blockSize, medium);
		}
		
		return count;
	}
	
	public long write (final InputStream stream, final IOByteBuffer medium) throws IOException {
		int          blockSize;
		final byte[] buffer;
		int          send;
		long         total = 0;
		
		buffer = new byte[this.bufferSize];
		
		for (;;) {
			blockSize = stream.read(buffer);
			
			if (blockSize == -1) {
				break;
			}
			
			medium.clear();
			medium.rewind();
			medium.putBytes(buffer, 0, blockSize);
			
			send = medium.write(this.channel);
			
			total += send;
		}
		
		return total;
	}
	
	public long write (final IOByteBuffer buffer) throws IOException {
		return this.write(buffer.array(), buffer.remaining(), buffer);
	}
}