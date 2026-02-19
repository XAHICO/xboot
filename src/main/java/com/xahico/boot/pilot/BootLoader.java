/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.pilot;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
final class BootLoader {
	private final List<Launchable> collection = new LinkedList<>();
	private Handler                handler = null;
	private int                    zindex = 0;
	
	
	
	BootLoader (){
		super();
	}
	
	
	
	public void add (final Launchable launchable){
		collection.add(launchable);
	}
	
	private boolean isDependenciesLoadedFor (final Launchable launchable){
		for (final var launchableOther : collection) {
			if (launchable.isDependentOf(launchableOther)) {
				return false;
			}
		}
		
		return true;
	}
	
	public void load (){
		final Iterator<Launchable> it;
		
		it = collection.iterator();
		
		while (it.hasNext()) {
			final Launchable launchable;
			
			launchable = it.next();
			
			if (launchable.isStandalone() || isDependenciesLoadedFor(launchable)) {
				zindex++;
				
				it.remove();
				
				load(launchable);
			}
		}
		
		if (! collection.isEmpty()) {
			this.load();
		}
	}
	
	private void load (final Launchable launchable){
		this.handler.call(launchable);
	}
	
	public void setHandler (final Handler handler){
		this.handler = handler;
	}
	
	public int steps (){
		return this.collection.size();
	}
	
	
	
	@FunctionalInterface
	public static interface Handler {
		void call (final Launchable launchable);
	}
}