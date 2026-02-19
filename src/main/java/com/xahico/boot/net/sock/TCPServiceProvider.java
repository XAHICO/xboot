/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.net.sock;

import com.xahico.boot.pilot.ServiceProvider;
import java.io.IOException;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public abstract class TCPServiceProvider extends ServiceProvider {
	private int bindPort = 0;
	
	
	
	protected TCPServiceProvider (){
		super();
	}
	
	
	
	public final int getBindPort (){
		return this.bindPort;
	}
	
	public abstract int getPort ();
	
	public final void setBindPort (final int port) throws IOException {
		this.bindPort = port;
	}
	
	@Override
	public String toString (){
		if (this.isStarted()) 
			return "%s (running at localhost:%d)".formatted(this.getClass().getName(), this.getPort());
		else {
			return "%s (not started (bind to localhost:%d))".formatted(this.getClass().getName(), this.getBindPort());
		}
	}
}