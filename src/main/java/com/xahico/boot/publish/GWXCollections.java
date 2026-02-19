/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.publish;

import com.xahico.boot.publish.collections.*;
import com.xahico.boot.util.ObjectInitializer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class GWXCollections {
	public static <T extends GWXNode> GWXNodeCollection<T> createFastCollection (final GWXNode owner){
		return createMapBackedConcurrentCollection(owner);
	}
	
	public static <T extends GWXNode> GWXNodeCollection<T> createFastOrderedCollection (final GWXNode owner){
		return createListBackedConcurrentCollection(owner);
	}
	
	public static <T extends GWXNode> GWXNodeCollection<T> createFastReadCollection (final GWXNode owner){
		return createSetBackedConcurrentCollection(owner);
	}
	
	public static <T extends GWXNode> GWXNodeCollection<T> createFastReadOnlyCollection (final GWXNode owner, final ObjectInitializer<Map<String, T>> initializer){
		return createMapBackedReadOnlyCollection(owner, initializer);
	}
	
	public static <T extends GWXNode> GWXNodeCollection<T> createFastReadOnlyCollection (final GWXNode owner, final Map<String, T> nodes){
		return createMapBackedReadOnlyCollection(owner, nodes);
	}
	
	public static <T extends GWXNode> GWXNodeCollection<T> createFastWriteCollection (final GWXNode owner){
		return createQueueBackedConcurrentCollection(owner);
	}
	
	public static <T extends GWXNode> GWXNodeCollection<T> createListBackedCollection (final GWXNode owner, final List<T> backing){
		return new GWXListBackedNodeCollection(backing);
	}
	
	public static <T extends GWXNode> GWXNodeCollection<T> createListBackedConcurrentCollection (final GWXNode owner){
		return new GWXListBackedConcurrentNodeCollection(owner);
	}
	
	public static <T extends GWXNode> GWXNodeCollection<T> createListBackedLockingCollection (final GWXNode owner){
		return createListBackedLockingCollection(owner, new ArrayList<>());
	}
	
	public static <T extends GWXNode> GWXNodeCollection<T> createListBackedLockingCollection (final GWXNode owner, final List<T> backing){
		return new GWXListBackedLockingNodeCollection(backing);
	}
	
	public static <T extends GWXNode> GWXNodeCollection<T> createListBackedReadOnlyCollection (final GWXNode owner){
		return createListBackedReadOnlyCollection(owner, new ArrayList<>());
	}
	
	public static <T extends GWXNode> GWXNodeCollection<T> createListBackedReadOnlyCollection (final GWXNode owner, final List<T> backing){
		return new GWXListBackedReadOnlyNodeCollection(backing);
	}
	
	public static <T extends GWXNode> GWXNodeCollection<T> createListBackedReadOnlyCollection (final GWXNode owner, final ObjectInitializer<List<T>> initializer){
		final List<T> nodes;
		
		nodes = new ArrayList<>();
		
		initializer.initialize(nodes);
		
		return createListBackedReadOnlyCollection(owner, nodes);
	}
	
	public static <T extends GWXNode> GWXNodeCollection<T> createMapBackedCollection (final GWXNode owner, final Map<String, T> backing){
		return new GWXMapBackedNodeCollection(backing);
	}
	
	public static <T extends GWXNode> GWXNodeCollection<T> createMapBackedConcurrentCollection (final GWXNode owner){
		return new GWXMapBackedConcurrentNodeCollection(owner);
	}
	
	public static <T extends GWXNode> GWXNodeCollection<T> createMapBackedLockingCollection (final GWXNode owner){
		return createMapBackedLockingCollection(owner, new HashMap<>());
	}
	
	public static <T extends GWXNode> GWXNodeCollection<T> createMapBackedLockingCollection (final GWXNode owner, final Map<String, T> backing){
		return new GWXMapBackedLockingNodeCollection(backing);
	}
	
	public static <T extends GWXNode> GWXNodeCollection<T> createMapBackedReadOnlyCollection (final GWXNode owner){
		return createMapBackedReadOnlyCollection(owner, new HashMap<>());
	}
	
	public static <T extends GWXNode> GWXNodeCollection<T> createMapBackedReadOnlyCollection (final GWXNode owner, final Map<String, T> backing){
		return new GWXMapBackedReadOnlyNodeCollection(backing);
	}
	
	public static <T extends GWXNode> GWXNodeCollection<T> createMapBackedReadOnlyCollection (final GWXNode owner, final ObjectInitializer<Map<String, T>> initializer){
		final Map<String, T> nodes;
		
		nodes = new HashMap<>();
		
		initializer.initialize(nodes);
		
		return createMapBackedReadOnlyCollection(owner, nodes);
	}
	
	public static <T extends GWXNode> GWXNodeCollection<T> createQueueBackedCollection (final GWXNode owner, final Queue<T> backing){
		return new GWXQueueBackedNodeCollection(backing);
	}
	
	public static <T extends GWXNode> GWXNodeCollection<T> createQueueBackedConcurrentCollection (final GWXNode owner){
		return new GWXQueueBackedConcurrentNodeCollection(owner);
	}
	
	public static <T extends GWXNode> GWXNodeCollection<T> createSetBackedCollection (final GWXNode owner, final Set<T> backing){
		return new GWXSetBackedNodeCollection(backing);
	}
	
	public static <T extends GWXNode> GWXNodeCollection<T> createSetBackedConcurrentCollection (final GWXNode owner){
		return new GWXSetBackedConcurrentNodeCollection(owner);
	}
	
	public static <T extends GWXNode> GWXNodeCollection<T> createSetBackedLockingCollection (final GWXNode owner){
		return createSetBackedLockingCollection(owner, new HashSet<>());
	}
	
	public static <T extends GWXNode> GWXNodeCollection<T> createSetBackedLockingCollection (final GWXNode owner, final Set<T> backing){
		return new GWXSetBackedLockingNodeCollection(backing);
	}
	
	public static <T extends GWXNode> GWXNodeCollection<T> createSetBackedReadOnlyCollection (final GWXNode owner){
		return createSetBackedReadOnlyCollection(owner, new HashSet<>());
	}
	
	public static <T extends GWXNode> GWXNodeCollection<T> createSetBackedReadOnlyCollection (final GWXNode owner, final Set<T> backing){
		return new GWXSetBackedReadOnlyNodeCollection(backing);
	}
	
	public static <T extends GWXNode> GWXNodeCollection<T> createSetBackedReadOnlyCollection (final GWXNode owner, final ObjectInitializer<Set<T>> initializer){
		final Set<T> nodes;
		
		nodes = new HashSet<>();
		
		initializer.initialize(nodes);
		
		return createSetBackedReadOnlyCollection(owner, nodes);
	}
	
	
	
	private GWXCollections (){
		throw new UnsupportedOperationException("Not supported.");
	}
}