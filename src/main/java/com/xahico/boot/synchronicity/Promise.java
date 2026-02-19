/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.synchronicity;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class Promise {
	private final Runnable callback;
	private boolean        complete = false;
	
	
	
	public Promise (){
		super();
		
		this.callback = null;
	}
	
	public Promise (final Runnable callback){
		super();
		
		this.callback = callback;
	}
	
	
	
	public void complete (){
		this.complete = true;
		
		if (null != this.callback) {
			this.callback.run();
		}
	}
	
	public boolean isComplete (){
		return this.complete;
	}
}