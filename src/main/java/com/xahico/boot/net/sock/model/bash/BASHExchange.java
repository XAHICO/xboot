/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.net.sock.model.bash;

import com.xahico.boot.io.IOByteBuffer;
import com.xahico.boot.io.IODynamicByteBuffer;
import com.xahico.boot.lang.jsox.JSOXException;
import com.xahico.boot.lang.jsox.JSOXObject;
import com.xahico.boot.lang.jsox.JSOXVariant;
import java.nio.charset.Charset;
import org.json.JSONObject;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class BASHExchange {
	private boolean              readComplete = false;
	private boolean              writeComplete = false;
	
	protected final IOByteBuffer bufferIn = new IODynamicByteBuffer();
	protected final IOByteBuffer bufferOut = new IODynamicByteBuffer();
	private final BASHConnection connection;
	
	
	
	BASHExchange (final BASHConnection connection){
		super();
		
		this.connection = connection;
	}
	
	
	
	public int available (){
		return this.bufferIn.length();
	}
	
	public void compact (){
		this.bufferIn.compact();
	}
	
	public void discard (){
		this.bufferIn.clear();
	}
	
	public void discard (final int count){
		this.bufferIn.discard(count);
	}
	
	private void ensureCanRead () throws BASHException {
		switch (connection.getMode()) {
			case TALK: {
				throw new StateError("mode set to %s: read operations are not allowed".formatted(connection.getMode()));
			}
			case TRANSACT: {
				if (! this.isWriteComplete()) {
					throw new StateError("mode set to %s: can not read before writing".formatted(connection.getMode()));
				}
				
				break;
			}
			case TRANSACT_REVERSED: {
				if (this.isWriteComplete()) {
					throw new StateError("mode set to %s: can not read after writing".formatted(connection.getMode()));
				}
				
				break;
			}
		}
	}
	
	private void ensureCanWrite () throws BASHException {
		switch (connection.getMode()) {
			case LISTEN: {
				throw new StateError("mode set to %s: write operations are not allowed".formatted(connection.getMode()));
			}
			case TRANSACT: {
				if (! this.isReadComplete()) {
					throw new StateError("mode set to %s: can not write before reading".formatted(connection.getMode()));
				}
				
				break;
			}
			case TRANSACT_REVERSED: {
				if (this.isReadComplete()) {
					throw new StateError("mode set to %s: can not read after writing".formatted(connection.getMode()));
				}
				
				break;
			}
		}
	}
	
	public boolean isReadComplete (){
		return this.readComplete;
	}
	
	public boolean isWriteComplete (){
		return this.writeComplete;
	}
	
	public void markReadComplete () throws BASHException {
		this.ensureCanRead();
		
		this.readComplete = true;
	}
	
	public void markWriteComplete () throws BASHException {
		this.ensureCanRead();
		
		this.writeComplete = true;
	}
	
	public byte[] read () throws BASHException {
		return this.read(this.available());
	}
	
	public byte[] read (final int count) throws BASHException {
		this.ensureCanRead();
		
		return this.bufferIn.getBytes(count);
	}
	
	public JSOXVariant readObject () throws BASHException {
		this.ensureCanRead();
		
		try {
			return this.bufferIn.getObject();
		} catch (final JSOXException ex) {
			throw new BASHException(ex);
		}
	}
	
	public <T extends JSOXObject> T readObject (final Class<T> jclass) throws BASHException {
		this.ensureCanRead();
		
		try {
			return this.bufferIn.getObject(jclass);
		} catch (final JSOXException ex) {
			throw new BASHException(ex);
		}
	}
	
	public String readString (final Charset charset) throws BASHException {
		this.ensureCanRead();
		
		return this.bufferIn.getString(charset);
	}
	
	public void reset (){
		this.bufferIn.clear();
		this.bufferOut.clear();
	}
	
	public void write (final byte[] packet) throws BASHException {
		this.write(packet, 0, packet.length);
	}
	
	public void write (final byte[] buffer, final int offset, final int count) throws BASHException {
		this.ensureCanWrite();
		
		this.bufferOut.putBytes(buffer, offset, count);
	}
	
	public void writeObject (final JSONObject object) throws BASHException {
		this.ensureCanWrite();
		
		try {
			this.bufferOut.putObject(object);
		} catch (final JSOXException ex) {
			throw new BASHException(ex);
		}
	}
	
	public void writeObject (final JSOXObject object) throws BASHException {
		this.ensureCanWrite();
		
		try {
			this.bufferOut.putObject(object);
		} catch (final JSOXException ex) {
			throw new BASHException(ex);
		}
	}
	
	public void writeString (final String data, final Charset charset) throws BASHException {
		this.ensureCanWrite();
		
		this.bufferOut.putString(data, charset);
	}
}