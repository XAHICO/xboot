/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.net.sock.model.asio;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public abstract class ASIOInstantObject {
	protected abstract boolean decode (final byte[] array);
	
	protected abstract byte[] encode ();
}
