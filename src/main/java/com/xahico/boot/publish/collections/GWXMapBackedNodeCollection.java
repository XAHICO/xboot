/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.publish.collections;

import com.xahico.boot.publish.GWXObject;
import com.xahico.boot.publish.GWXUtilities;
import com.xahico.boot.util.OrderedConsumer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * TBD.
 * 
 * @param <T> 
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public class GWXMapBackedNodeCollection <T extends GWXObject> extends GWXMemoryNodeCollectionBase<T> {
	protected final Map<String, T> nodes;
	
	
	
	public GWXMapBackedNodeCollection (final Map<String, T> nodes){
		super();
		
		this.nodes = nodes;
	}
	
	public GWXMapBackedNodeCollection (final GWXObject owner, final Map<String, T> nodes){
		super(owner);
		
		this.nodes = nodes;
	}
	
	
	
	@Override
	public void add (final T node){
		final Object nodeId;
		final T      ov;
		
		if (null != node) {
			nodeId = GWXUtilities.getNodeId(node);
			
			if (null != nodeId) {
				ov = nodes.put(Objects.toString(nodeId), node);
				
				if (ov != node) {
					this.bind(node);
				}
			}
		}
	}
	
	@Override
	public void addAll (final Collection<T> nodes){
		final Map<String, T> medium;
		
		medium = new HashMap<>();
		
		for (final var node : nodes) {
			final Object nodeId;
			
			if (node == null) 
				continue;
			
			nodeId = GWXUtilities.getNodeId(node);
			
			if (nodeId == null) 
				continue;
			
			medium.put(Objects.toString(nodeId), node);
			
			this.bind(node);
		}
		
		if (! medium.isEmpty()) {
			this.nodes.putAll(medium);
		}
	}
	
	@Override
	public void clear (){
		nodes.values().forEach(node -> this.unbind(node));
		
		nodes.clear();
	}
	
	@Override
	public boolean contains (final T node){
		if (null != node) {
			return nodes.containsKey(Objects.toString(GWXUtilities.getNodeId(node)));
		} else {
			return false;
		}
	}

	@Override
	public T lookup (final Object key){
		if (null != key) {
			return nodes.get(Objects.toString(key));
		} else {
			return null;
		}
	}

	@Override
	public void remove (final T node){
		final String key;
		final T      ov;
		
		if (null != node) {
			key = Objects.toString(GWXUtilities.getNodeId(node));

			ov = nodes.remove(key);

			if (ov == node) {
				this.unbind(node);
			} else {
				nodes.put(key, ov);
			}
		}
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