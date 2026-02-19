/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.xahico.boot.net.sock;

import com.xahico.boot.net.inet.InetEndpoint;
import java.io.IOException;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public interface TCPSocketConnection extends SocketConnection {
	boolean canReconnect ();
	
	void connect (final InetEndpoint hostname) throws IOException;
	
	void connect (final String hostname) throws IOException;
	
	void connect (final String address, final int port) throws IOException;
	
	void reconnect () throws IllegalStateException;
	
	@SuppressWarnings("SleepWhileInLoop")
	void reconnect (final int intervalMillis) throws IllegalStateException;
}