/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.xahico.boot.net.sock;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
@FunctionalInterface
public interface SocketConnector {
	void connect (final Socket socket, final SocketAddress hostname, final int timeout) throws IOException;
}