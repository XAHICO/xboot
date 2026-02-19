/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.publish.collections;

import com.xahico.boot.publish.GWXObject;
import com.xahico.boot.publish.GWXUtilities;
import com.xahico.boot.util.OrderedConsumer;
import java.util.Set;

/**
 * TBD.
 * 
 * @param <T> 
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public class GWXSetBackedReadOnlyNodeCollection <T extends GWXObject> extends GWXReadOnlyNodeCollection<T> {
	protected final Set<T> nodes;
	
	
	
	public GWXSetBackedReadOnlyNodeCollection (final Set<T> nodes){
		super();
		
		this.nodes = nodes;
	}
	
	public GWXSetBackedReadOnlyNodeCollection (final GWXObject owner, final Set<T> nodes){
		super(owner);
		
		this.nodes = nodes;
	}
	
	
	
	@Override
	public boolean contains (final T node){
		return nodes.contains(node);
	}
	
	@Override
	public T lookup (final Object key){
		for (final var node : nodes) {
			if (GWXUtilities.checkNodeKey(node, key)) {
				return node;
			}
		}
		
		return null;
	}
	
	@Override
	public int size (){
		return nodes.size();
	}
	
	@Override
	public void walk (final OrderedConsumer<T> consumer) {
		for (final var node : nodes) {
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