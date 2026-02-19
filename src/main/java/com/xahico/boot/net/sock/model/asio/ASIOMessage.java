/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.net.sock.model.asio;

import com.xahico.boot.lang.jsox.JSOXException;
import com.xahico.boot.lang.jsox.JSOXVariant;
import com.xahico.boot.util.StringUtilities;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public abstract class ASIOMessage {
	public static ASIOMessage wrapBytes (final byte[] bytes){
		return new ASIOMessage() {
			@Override
			public int size (){
				return bytes.length;
			}
			
			@Override
			public InputStream stream (){
				return new ByteArrayInputStream(bytes);
			}
		};
	}
	
	public static ASIOMessage wrapObject (final JSOXVariant object, final Charset charset) throws JSOXException {
		return ASIOMessage.wrapString(object.toString(), charset);
	}
	
	public static ASIOMessage wrapString (final String string, final Charset charset){
		return ASIOMessage.wrapBytes(StringUtilities.bytes(string, charset, true));
	}
	
	
	
	private Runnable callback = null;
	
	
	
	private ASIOMessage (){
		super();
	}
	
	
	
	public final void fireCallback (){
		if (null != this.callback) {
			this.callback.run();
		}
	}
	
	public final void setCallback (final Runnable callback){
		this.callback = callback;
	}
	
	public abstract int size ();
	
	public abstract InputStream stream () throws IOException;
}