/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.net.sock.model.fts;

import com.xahico.boot.platform.FileUtilities;
import com.xahico.boot.util.Exceptions;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
final class FTSUpload {
	private static final long EXPIRATION_TIMEOUT = ((1000 * 60) * 5); // 5 minutes
	
	
	
	private boolean                  abandoned = false;
	private boolean                  accepted = false;
	private final FTSUploadCallbacks callbacks;
	private boolean                  completed = false;
	private boolean                  initialized = false;
	private final String             key;
	private final File               path;
	private long                     position = 0;
	private long                     size = 0;
	private OutputStream             stream = null;
	private long                     whenAbandoned = -1;
	
	
	
	FTSUpload (final File directory, final String key, final FTSUploadCallbacks callbacks){
		super();
		
		this.key = key;
		this.path = new File(directory, this.key + ".zip");
		this.callbacks = callbacks;
	}
	
	
	
	public boolean canDiscard (){
		if (this.isCompleted()) 
			return true;
		
		if (this.isAbandoned() && ((System.currentTimeMillis() - this.whenAbandoned) > EXPIRATION_TIMEOUT)) {
			return true;
		} else {
			return false;
		}
	}
	
	public boolean canWrite (){
		return (null != stream);
	}
	
	public boolean close (){
		if (null != this.stream) try {
			this.stream.close();
			
			return true;
		} catch (final IOException ex) {
			Exceptions.ignore(ex);
		}
		
		return false;
	}
	
	void complete (){
		if (null != this.stream) try {
			this.stream.close();
			this.stream = null;
		} catch (final IOException ex) {
			Exceptions.ignore(ex);
		}
		
		this.callbacks.complete(this.key, this.path);
	}
	
	public void discard (){
		if (null != this.stream) try {
			this.stream.close();
			this.stream = null;
		} catch (final IOException ex) {
			Exceptions.ignore(ex);
		}
		
		if (this.path.exists()) try {
			FileUtilities.delete(this.path);
		} catch (final IOException ex) {
			Exceptions.ignore(ex);
		}
		
		this.callbacks.discard(this.key, this.path);
	}
	
	public void initialize (final long size){
		this.size = size;
		
		this.initialized = true;
	}
	
	public boolean isAbandoned (){
		return this.abandoned;
	}
	
	public boolean isAccepted (){
		return this.accepted;
	}
	
	public boolean isCompleted (){
		return this.completed;
	}
	
	public boolean isInitialized (){
		return this.initialized;
	}
	
	public String key (){
		return this.key;
	}
	
	public void markAbandoned (final boolean abandoned){
		this.abandoned = abandoned;
		
		if (this.abandoned) {
			this.whenAbandoned = System.currentTimeMillis();
		} else {
			this.whenAbandoned = -1;
		}
	}
	
	public void markAccepted (final boolean accepted){
		this.accepted = accepted;
	}
	
	public void markCompleted (){
		this.completed = true;
	}
	
	public void open (){
		try {
			this.stream = new FileOutputStream(this.path);
		} catch (final FileNotFoundException ex) {
			throw new Error(ex);
		}
	}
	
	public long position (){
		return this.position;
	}
	
	public long remaining (){
		return (this.size - this.position);
	}
	
	public long size (){
		return this.size;
	}
	
	public void write (final byte[] block, final int actualSize){
		//System.out.println("Writing [[%s]]".formatted(new String(Arrays.copyOf(block, actualSize), UTF_8)));
		try {
			this.stream.write(block, 0, actualSize);
			
			this.stream.flush();
			
			this.position += actualSize;
		} catch (final IOException ex) {
			throw new Error(ex);
		}
	}
}