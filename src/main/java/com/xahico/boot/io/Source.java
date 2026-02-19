/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.io;

import com.xahico.boot.dev.Helper;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public interface Source extends Closeable {
	@Helper
	public static Source wrapFile (final File file){
		return Source.wrapFile(file.getPath());
	}
	
	@Helper
	public static Source wrapFile (final Path filePath){
		return Source.wrapFile(filePath.toString());
	}
	
	public static Source wrapFile (final String filePath){
		return new Source() {
			InputStream stream = null;
			
			@Override
			public void close () throws IOException {
				if (null != this.stream) {
					this.stream.close();
				}
			}
			
			@Override
			public void open () throws IOException {
				this.stream = new FileInputStream(filePath);
			}
			
			@Override
			public InputStream stream (){
				return this.stream;
			}
		};
	}
	
	public static Source wrapString (final String string){
		return new Source() {
			InputStream stream = null;
			
			@Override
			public void close () throws IOException {
				if (null != this.stream) {
					this.stream.close();
				}
			}
			
			@Override
			public void open () throws IOException {
				try{
				this.stream = new ByteArrayInputStream(string.getBytes());
				}catch(Throwable t) {
					t.printStackTrace();
				}
			}
			
			@Override
			public InputStream stream (){
				return this.stream;
			}
		};
	}
	
	
	
	void open () throws IOException;
	
	InputStream stream ();
}