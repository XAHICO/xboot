/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.xahico.boot.util.async;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * TBD. 
 * 
 * @author KARBAROTTA
**/
public class MonitorSet implements Iterable<Monitor> {
	private final Set<Monitor> hooks = new HashSet<>();
	
	
	
	public MonitorSet (){
		super();
	}
	
	
	
	public boolean add (final Monitor hook){
		return hooks.add(hook);
	}
	
	public void clear (){
		hooks.clear();
	}
	
	public boolean contains (final Object hook){
		if (hook instanceof Monitor) 
			return MonitorSet.this.contains((Monitor) hook);
		else {
			return false;
		}
	}
	
	public boolean contains (final Monitor hook){
		return hooks.contains(hook);
	}
	
	public void fireAll (final Monitor.State state){
		hooks.forEach(hook -> hook.enterState(state));
	}
	
	@Override
	public Iterator<Monitor> iterator (){
		return hooks.iterator();
	}
	
	public boolean remove (final Object hook){
		if (hook instanceof Monitor) 
			return MonitorSet.this.remove((Monitor) hook);
		else {
			return false;
		}
	}
	
	public boolean remove (final Monitor hook){
		return hooks.remove(hook);
	}
	
	public int size (){
		return hooks.size();
	}
}