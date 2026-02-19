/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.synchronicity;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public interface Synchronizable {
	/**
	 * Call a routine in synchronicity with this {@code Synchronizable}.
	 * 
	 * The routine will be executed sometime in the future in 
	 * guaranteed synchronicity.
	 * 
	 * @param routine 
	 * Routine to run.
	**/
	void call (final Runnable routine);
	
	void call (final ScheduledRunnable routine);
	
	<T> Future<T> call (final Callable<T> task);
	
	Executor getExecutor ();
	
	void setExecutor (final Executor executor);
}