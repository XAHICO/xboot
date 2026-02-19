/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.xahico.boot.publish;

import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class GWXResourceAccessStore {
	private final Set<GWXPermission> accessModes = new CopyOnWriteArraySet<>();
	
	
	
	GWXResourceAccessStore (){
		super();
	}
	
	
	
	public boolean add (final GWXPermission accessMode){
		return this.accessModes.add(accessMode);
	}
	
	public boolean addAll (final Set<GWXPermission> accessModes){
		return this.accessModes.addAll(accessModes);
	}
	
	public boolean contains (final GWXPermission accessMode){
		return this.accessModes.contains(accessMode);
	}
	
	public boolean remove (final GWXPermission accessMode){
		return this.accessModes.remove(accessMode);
	}
	
	public boolean removeAll (final Set<GWXPermission> accessModes){
		return this.accessModes.removeAll(accessModes);
	}
}