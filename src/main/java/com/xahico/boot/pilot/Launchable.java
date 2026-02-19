/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.pilot;

import com.xahico.boot.util.ArrayUtilities;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public abstract class Launchable {
	private final Class<?> instanceClass;
	
	
	
	Launchable (final Class<?> instanceClass){
		super();
		
		this.instanceClass = instanceClass;
	}
	
	
	
	public abstract Class<?>[] getDependencies ();
	
	public final Class<?> getInstanceClass (){
		return this.instanceClass;
	}
	
	public final boolean hasDependencies (){
		return (this.getDependencies().length > 0);
	}
	
	public final boolean isDependencyOf (final Launchable other){
		return other.isDependentOf(this);
	}
	
	public final boolean isDependentOf (final Launchable other){
		return ArrayUtilities.contains(this.getDependencies(), other.instanceClass);
	}
	
	public final boolean isStandalone (){
		return !this.hasDependencies();
	}
	
	public abstract void start ();
	
	public abstract void stop ();
	
	@Override
	public String toString (){
		return "%s (%s)".formatted(Instance.class, this.getInstanceClass());
	}
}