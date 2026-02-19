/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.publish.collections;

import com.xahico.boot.publish.GWXObject;
import com.xahico.boot.publish.GWXUtilities;
import com.xahico.boot.util.OrderedConsumer;
import java.util.Collection;
import java.util.Queue;

/**
 * TBD.
 * 
 * @param <T> 
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public class GWXQueueBackedNodeCollection <T extends GWXObject> extends GWXMemoryNodeCollectionBase<T> {
	protected final Queue<T> nodes;
	
	
	
	public GWXQueueBackedNodeCollection (final Queue<T> nodes){
		super();
		
		this.nodes = nodes;
	}
	
	public GWXQueueBackedNodeCollection (final GWXObject owner, final Queue<T> nodes){
		super(owner);
		
		this.nodes = nodes;
	}
	
	
	
	@Override
	public void add (final T node){
		if (nodes.offer(node)) {
			this.bind(node);
		}
	}
	
	@Override
	public void addAll (final Collection<T> nodes){
		this.nodes.addAll(nodes);
		
		nodes.forEach(node -> this.bind(node));
	}
	
	@Override
	public void clear (){
		nodes.forEach((node) -> this.unbind(node));
		
		nodes.clear();
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
	public void remove (final T node){
		if (nodes.remove(node)) {
			this.unbind(node);
		}
	}

	@Override
	public void removeAll (final Collection<T> nodes){
		this.nodes.removeAll(nodes);
		
		nodes.forEach(node -> this.unbind(node));
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