/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.xahico.boot.net.sock;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public interface TCPSocketConnector extends SocketConnector {
	void connect (final Socket socket, final InetSocketAddress hostname, final int timeout) throws IOException;
}