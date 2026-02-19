/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.publish.collections;

import com.xahico.boot.publish.GWXObject;
import com.xahico.boot.publish.GWXUtilities;
import com.xahico.boot.util.OrderedConsumer;
import java.util.Map;

/**
 * TBD.
 * 
 * @param <T> 
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public class GWXMapBackedReadOnlyNodeCollection <T extends GWXObject> extends GWXReadOnlyNodeCollection<T> {
	protected final Map<Object, T> nodes;
	
	
	
	public GWXMapBackedReadOnlyNodeCollection (final Map<Object, T> nodes){
		super();
		
		this.nodes = nodes;
	}
	
	public GWXMapBackedReadOnlyNodeCollection (final GWXObject owner, final Map<Object, T> nodes){
		super(owner);
		
		this.nodes = nodes;
	}
	
	
	
	@Override
	public boolean contains (final T node){
		return nodes.containsKey(GWXUtilities.getNodeId(node));
	}
	
	@Override
	public T lookup (final Object key){
		return nodes.get(key);
	}
	
	@Override
	public int size (){
		return nodes.size();
	}
	
	@Override
	public void walk (final OrderedConsumer<T> consumer) {
		for (final var node : nodes.values()) {
			if (! consumer.accept(node)) {
				break;
			}
		}
	}

	@Override
	public void walkReversed (final OrderedConsumer<T> consumer) {
		throw new UnsupportedOperationException("Not supported.");
	}
}