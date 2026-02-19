/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.publish.collections;

import com.xahico.boot.publish.GWXObject;
import com.xahico.boot.util.OrderedConsumer;
import java.util.List;

/**
 * TBD.
 * 
 * @param <T> 
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public class GWXListBackedLockingNodeCollection <T extends GWXObject> extends GWXListBackedNodeCollection<T> {
	public GWXListBackedLockingNodeCollection (final List<T> backing){
		super(backing);
	}
	
	public GWXListBackedLockingNodeCollection (final GWXObject owner, final List<T> backing){
		super(owner, backing);
	}
	
	
	
	@Override
	public boolean contains (final T node){
		synchronized (nodes) {
			return super.contains(node);
		}
	}
	
	@Override
	public T lookup (final Object key){
		synchronized (nodes) {
			return super.lookup(key);
		}
	}
	
	@Override
	public int size (){
		synchronized (nodes) {
			return super.size();
		}
	}
	
	@Override
	public void walk (final OrderedConsumer<T> consumer) {
		synchronized (nodes) {
			super.walk(consumer);
		}
	}
}