/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.xahico.boot.io;

import com.xahico.boot.dev.Helper;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public interface IOReadable {
	boolean canRead ();
	
	@Helper
	char getChar ();
	
	char getChar (final Charset charset);
	
	double getDouble ();
	
	int getInteger ();
	
	long getLong ();
	
	@Helper
	String getString ();
	
	String getString (final Charset charset);
	
	int read (final SocketChannel channel) throws IOException;
	
	@Helper
	default int read (final IOSocketChannel channel) throws IOException {
		return this.read(channel.wrappedChannel());
	}
}