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
public interface IOWritable {
	boolean canWrite ();
	
	IOWritable putByte (final int val);
	
	IOWritable putBytes (final byte[] val);
	
	IOWritable putBytes (final byte[] val, final int offset, final int count);
	
	IOWritable putChar (final char val);
	
	IOWritable putDouble (final double val);
	
	IOWritable putInteger (final int val);
	
	IOWritable putLong (final long val);
	
	IOWritable putString (final String val);
	
	IOWritable putString (final String val, final Charset charset);
	
	int write (final SocketChannel channel) throws IOException;
	
	@Helper
	default int write (final IOSocketChannel channel) throws IOException {
		return this.write(channel.wrappedChannel());
	}
}