/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.xahico.boot.net.sock;

import com.xahico.boot.net.Connection;
import java.io.IOException;
import java.net.SocketAddress;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public interface SocketConnection extends Connection {
	void connect (final SocketAddress hostname) throws IOException;
	
	void connect (final SocketAddress hostname, final int timeout) throws IOException;
}