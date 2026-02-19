/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.net;

import com.xahico.boot.pilot.Session;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public abstract class NetSession extends Session {
	protected NetSession (){
		super();
	}
	
	
	
	public abstract void disconnect ();
	
	public abstract boolean isDisconnected ();
	
	protected abstract void onConnect ();
	
	protected abstract void onDisconnect ();
}