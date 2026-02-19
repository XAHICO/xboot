/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.publish;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class GWXHook {
	private final Runnable    callback;
	private final GWXSession  target;
	private final GWXHookType type;
	
	
	
	GWXHook (final GWXSession target, final GWXHookType type, final Runnable callback){
		super();
		
		this.target = target;
		this.type = type;
		this.callback = callback;
	}
	
	
	
	public void unhook (){
		this.target.unregisterHook(this.type, this.callback);
	}
}