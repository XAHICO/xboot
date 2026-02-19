/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public class ReverseFileInputStream extends InputStream {
	private static final int DEFAULT_BUFFER_SIZE = 1;
	
	
	
	private int                    available = 0;
	private byte[]                 buffer = null;
	private int                    cursor = 0;
	private long                   depth;
	private final RandomAccessFile rf;
	private final long             total;
	
	
	
	public ReverseFileInputStream (final File file) throws IOException {
		this(file, DEFAULT_BUFFER_SIZE);
	}
	
	public ReverseFileInputStream (final File file, final int bufferSize) throws IOException {
		super();
		
		this.total = file.length();
		this.depth = ((bufferSize < this.total) ? (this.total - (bufferSize - 1)) : 1);
		this.rf = new RandomAccessFile(file, "r");
		this.buffer = this.createBuffer(bufferSize);
	}
	
	
	
	@Override
	public void close () throws IOException {
		this.rf.close();
	}
	
	private byte[] createBuffer (final int bufferSize){
		return new byte[bufferSize];
	}
	
	public long position (){
		return this.depth;
	}
	
	public void position (final long newPosition) throws IOException {
		if (newPosition == -1) {
			this.depth = ((this.buffer.length < this.total) ? (this.total - (this.buffer.length - 1)) : 1);
		} else {
			this.depth = newPosition;
		}
		
		this.rf.seek(this.depth);
	}
	
	@Override
	public int read () throws IOException {
		if ((this.available <= 0) || (this.cursor == -1)) {
			if (this.depth <= 0) {
				return -1;
			} else {
				this.rf.seek(this.depth - 1);
				this.available = this.rf.read(this.buffer, 0, this.buffer.length);
				this.cursor = (this.available - 1);
				
				if (this.available == -1) {
					return -1;
				} else {
					this.depth -= this.available;
				}
			}
		}
		
		try {
			return this.buffer[this.cursor];
		} finally {
			this.cursor--;
		}
	}
	
	public String readLineUTF8 () throws IOException {
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
	public final long total (){
		return this.total;
	}
}