/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.publish.collections;

import com.xahico.boot.publish.GWXNodeCollection;
import com.xahico.boot.publish.GWXObject;
import com.xahico.boot.publish.GWXUtilities;
import com.xahico.boot.util.OrderedConsumer;

/**
 * TBD.
 * 
 * @param <T> 
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public abstract class GWXProxyNodeCollection <T extends GWXObject> extends GWXNodeCollection<T> {
	private final GWXNodeCollection<T> idol;
	
	
	
	protected GWXProxyNodeCollection (final GWXNodeCollection idol){
		super(GWXUtilities.getObjectParent(idol));
		
		this.idol = idol;
	}
	
	
	
	@Override
	public void add (final T node){
		this.idol.add(node);
	}
	
	@Override
	public void clear (){
		this.idol.clear();
	}
	
	@Override
	public boolean contains (final T node){
		return this.idol.contains(node);
	}

	@Override
	public T lookup (final Object key){
		return this.idol.lookup(key);
	}

	@Override
	public void remove (final T node){
		this.idol.remove(node);
	}

	@Override
	public int size (){
		return this.idol.size();
	}

	@Override
	public void walk (final OrderedConsumer<T> consumer) {
		this.idol.walk(consumer);
	}

	@Override
	public void walkReversed (final OrderedConsumer<T> consumer) {
		this.idol.walkReversed(consumer);
	}
}