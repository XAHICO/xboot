/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.xahico.boot.io;

import com.xahico.boot.dev.Helper;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public interface IOBuffer extends IOReadable, IOWritable {
	Charset DEFAULT_CHARSET = StandardCharsets.UTF_16BE;
	
	
	
	Charset charset ();
	
	IOBuffer clear ();
	
	IOBuffer discard (final int count);
	
	@Helper
	default int find (final int b){
		return this.find(b, 0);
	}
	
	int find (final int b, final int fromIndex);
	
	boolean isFull ();
	
	int length ();
	
	@Helper
	default String substring (final int offset){
		return this.substring(offset, this.length(), this.charset());
	}
	
	@Helper
	default String substring (final int offset, final int length){
		return this.substring(offset, length, this.charset());
	}
	
	String substring (final int offset, final int length, final Charset charset);
	
	@Helper
	default String substring (final int offset, final Charset charset){
		return this.substring(offset, this.length(), charset);
	}
	
	@Helper
	default byte[] toByteArray (){
		return this.toByteArray(this.charset());
	}
	
	byte[] toByteArray (final Charset charset);
	
	String toString (final Charset charset);
}