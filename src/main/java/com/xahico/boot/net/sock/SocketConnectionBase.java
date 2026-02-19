/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.net.sock;

import com.xahico.boot.util.Exceptions;
import com.xahico.boot.net.ConnectionBase;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public abstract class SocketConnectionBase extends ConnectionBase implements TCPSocketConnection {
	public static final int DEFAULT_RECONNECT_INTERVAL = 1000;
	
	
	
	private SocketAddress   connectName = null;
	private Socket          socket = null;
	private SocketConnector socketConnector = null;
	
	
	
	public SocketConnectionBase (){
		super();
	}
	
	protected SocketConnectionBase (final Socket socket){
		super();
		
		this.socket = socket;
	}
	
	
	
	@Override
	public final boolean canReconnect (){
		return (null != this.connectName);
	}
	
	@Override
	public void close (){
		if (null != this.socket) try {
			this.socket.close();
		} catch (final IOException ex) {
			Exceptions.ignore(ex);
		} finally {
			this.socket = null;
		}
	}
	
	@Override
	public final void connect (final SocketAddress hostname) throws IOException {
		this.connect(hostname, 0);
	}
	
	@Override
	public final void connect (final SocketAddress hostname, final int timeout) throws IOException {
		try {
			this.socket = this.createSocket();
			this.socketConnector.connect(this.socket, hostname, timeout);
			this.connectName = hostname;
			this.openChannels(this.socket);
		} catch (final IOException ex) {
			this.socket = null;
			
			throw ex;
		}
	}
	
	protected Socket createSocket () throws IOException {
		return new Socket();
	}
	
	protected void openChannels (final Socket socket) throws IOException {
		
	}
	
	@Override
	public final void reconnect () throws IllegalStateException {
		this.reconnect(DEFAULT_RECONNECT_INTERVAL);
	}
	
	@Override
	@SuppressWarnings("SleepWhileInLoop")
	public final void reconnect (final int intervalMillis) throws IllegalStateException {
		if (null == this.connectName) {
			throw new IllegalStateException("no previously connected hostname to connect to: try canReconnect() first");
		}
		
		for (;;) try {
			this.connect(this.connectName);
		} catch (final IOException ex) {
			Exceptions.ignore(ex);
			
			try {
				Thread.sleep(intervalMillis);
			} catch (final InterruptedException __) {
				Exceptions.ignore(__);
			}
		}
	}
	
	public final void setConnector (final SocketConnector connector){
		this.socketConnector = connector;
	}
}