/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.publish;

import java.util.concurrent.Executor;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class GWXConnectionConfiguration {
	private Executor executor = null;
	private int      requestVersion = 0;
	private boolean  useTLS = false;
	
	
	
	GWXConnectionConfiguration (){
		super();
	}
	
	
	
	public Executor getExecutor (){
		return this.executor;
	}
	
	public int getRequestVersion (){
		return this.requestVersion;
	}
	
	public boolean getUseTLS (){
		return this.useTLS;
	}
	
	public void setExecutor (final Executor executor){
		this.executor = executor;
	}
	
	public void setRequestVersion (final int requestVersion){
		this.requestVersion = requestVersion;
	}
	
	public void setUseTLS (final boolean useTLS){
		this.useTLS = useTLS;
	}
}