/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.net.sock;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import javax.net.ssl.SSLContext;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public class TCPSocketServer {
	private ServerSocket socket = null;
	private SSLContext   sslContext = null;
	
	
	
	public TCPSocketServer (){
		super();
	}
	
	
	
	public TCPSocket accept () throws IOException {
		return TCPSocket.wrap(this.socket.accept());
	}
	
	public SSLContext getSSLContext (){
		return this.sslContext;
	}
	
	public boolean isSecure (){
		return (null != this.sslContext);
	}
	
	public void listen (final int bindPort) throws IOException {
		if (this.isSecure()) {
			this.socket = this.sslContext.getServerSocketFactory().createServerSocket();
		} else {
			this.socket = new ServerSocket();
		}
		
		try {
			this.socket.bind(InetSocketAddress.createUnresolved("localhost", bindPort));
		} catch (final IOException ex) {
			this.socket = null;
			
			throw ex;
		}
	}
	
	public void setSSLContext (final SSLContext sslContext){
		this.sslContext = sslContext;
	}
}