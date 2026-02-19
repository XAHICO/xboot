/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.pilot;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class Service extends Launchable {
	private boolean               loaded = false;
	
	private final Class<?>[]      dependencies;
	private final ServiceProvider provider;
	
	
	
	Service (final Class<?> instanceClass, final ServiceProvider provider, final Class<?>[] dependencies){
		super(instanceClass);
		
		this.provider = provider;
		this.dependencies = dependencies;
	}
	
	
	
	@Override
	public Class<?>[] getDependencies (){
		return this.dependencies;
	}
	
	public ServiceProvider getProvider (){
		return this.provider;
	}
	
	public boolean isLoaded (){
		return this.loaded;
	}
	
	public boolean load (){
		if (this.loaded == true) {
			return false;
		}
		
		try {
			this.provider.start();
			
			return this.loaded = true;
		} catch (final Error err) {
			return this.loaded = false;
		}
	}
	
	@Override
	public void start (){
		this.getProvider().start();
	}
	
	@Override
	public void stop (){
		this.getProvider().stop();
	}
	
	@Override
	public String toString (){
		return "%s (%s)".formatted(this.getInstanceClass(), this.provider);
	}
}