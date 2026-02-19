/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.net.sock.model.bash;

import com.xahico.boot.lang.jsox.JSOXException;
import com.xahico.boot.lang.jsox.JSOXObject;
import com.xahico.boot.lang.jsox.JSOXUtilities;
import com.xahico.boot.lang.jsox.JSOXVariant;
import com.xahico.boot.util.StringUtilities;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import org.json.JSONObject;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public abstract class BASHMessage {
	public static BASHMessage wrapBytes (final byte[] bytes){
		return new BASHMessage() {
			@Override
			public long size (){
				return bytes.length;
			}
			
			@Override
			public InputStream stream (){
				return new ByteArrayInputStream(bytes);
			}
		};
	}
	
	public static BASHMessage wrapFile (final File file){
		return new BASHMessage() {
			@Override
			public long size (){
				return file.length();
			}
			
			@Override
			public InputStream stream () throws IOException {
				return new FileInputStream(file);
			}
		};
	}
	
	public static BASHMessage wrapObject (final JSONObject object, final Charset charset) throws JSOXException {
		return wrapObject(new JSOXVariant(object), charset);
	}
	
	public static BASHMessage wrapObject (final JSOXObject object, final Charset charset) throws JSOXException {
		return BASHMessage.wrapBytes(JSOXUtilities.encodeObject(object, charset));
	}
	
	public static BASHMessage wrapObject (final JSOXVariant object, final Charset charset) throws JSOXException {
		return BASHMessage.wrapBytes(JSOXUtilities.encodeObject(object, charset));
	}
	
	public static BASHMessage wrapString (final String string, final Charset charset){
		return BASHMessage.wrapBytes(StringUtilities.bytes(string, charset, true));
	}
	
	
	
	private Runnable callback = null;
	
	
	
	private BASHMessage (){
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
	
	public abstract long size ();
	
	public abstract InputStream stream () throws IOException;
}