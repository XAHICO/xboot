/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.publish.collections;

import com.xahico.boot.publish.GWXObject;
import com.xahico.boot.publish.GWXUtilities;
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
public class GWXListBackedReadOnlyNodeCollection <T extends GWXObject> extends GWXReadOnlyNodeCollection<T> {
	protected final List<T> nodes;
	
	
	
	public GWXListBackedReadOnlyNodeCollection (final List<T> nodes){
		super();
		
		this.nodes = nodes;
	}
	
	public GWXListBackedReadOnlyNodeCollection (final GWXObject owner, final List<T> nodes){
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
		for (var i = (nodes.size() - 1); i >= 0; i--) {
			final T node;
			
			node = nodes.get(i);
			
			if (! consumer.accept(node)) {
				break;
			}
		}
	}
}