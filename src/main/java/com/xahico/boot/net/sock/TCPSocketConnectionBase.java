/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.net.sock;

import com.xahico.boot.net.inet.InetEndpoint;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public abstract class TCPSocketConnectionBase extends SocketConnectionBase {
	public TCPSocketConnectionBase (){
		super();
	}
	
	protected TCPSocketConnectionBase (final Socket socket){
		super(socket);
	}
	
	
	
	@Override
	public final void connect (final InetEndpoint hostname) throws IOException {
		this.connect(hostname.getAddressString(), hostname.getPort());
	}
	
	@Override
	public final void connect (final String hostname) throws IOException {
		this.connect(InetEndpoint.getByName(hostname));
	}
	
	@Override
	public final void connect (final String address, final int port) throws IOException {
		this.connect(new InetSocketAddress(address, port));
	}
}