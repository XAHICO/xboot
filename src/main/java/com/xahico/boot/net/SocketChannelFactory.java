/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.xahico.boot.net;

import java.io.IOException;
import java.nio.channels.SocketChannel;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
@FunctionalInterface
public interface SocketChannelFactory {
	SocketChannel newInstance () throws IOException;
}