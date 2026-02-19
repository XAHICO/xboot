/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.pilot;

import com.xahico.boot.synchronicity.SynchronizationHandler;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public abstract class Session extends SynchronizationHandler {
	protected Session (){
		super();
	}
	
	
	
	/**
	 * Callback on creation.
	 * <br>
	 * Calling 
	 * {@link com.xahico.boot.synchronicity.Synchronized 
	 * implicitly synchronized} or 
	 * {@link #call(java.util.concurrent.Callable)  
	 * explicitly synchronized (with return)} methods
	 * from the callback will result in deadlock and should be avoided 
	 * indefinitely.
	**/
	protected abstract void onCreate ();
	
	/**
	 * Callback on creation.
	 * <br>
	 * Calling 
	 * {@link com.xahico.boot.synchronicity.Synchronized 
	 * implicitly synchronized} or 
	 * {@link #call(java.util.concurrent.Callable)  
	 * explicitly synchronized (with return)} methods
	 * from the callback will result in deadlock and should be avoided 
	 * indefinitely.
	**/
	protected abstract void onDestroy ();
}