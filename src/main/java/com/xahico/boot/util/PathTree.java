/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.util;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;

/**
 * TBD.
 * 
 * @param <T> 
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class PathTree <T> {
	private static boolean containsWildcard (final String s){
		return (s.indexOf('*') != -1);
	}
	
	
	
	public static final int CACHE_SIZE_BIG = 100000;
	public static final int CACHE_SIZE_HUGE = 1000000;
	public static final int CACHE_SIZE_MEDIUM = 5000;
	public static final int CACHE_SIZE_SMALL = 250;
	
	
	
	public static PathTree createDefaultPathTree (){
		return PathTree.createPathTree(new Factory() {
			@Override
			public Map newMap (){
				return new HashMap<>();
			}

			@Override
			public Set newSet (){
				return new HashSet();
			}
		});
	}
	
	public static PathTree createPathTree (final Factory factory){
		return new PathTree(factory);
	}
	
	public static PathTree createSynchronizedPathTree (){
		return PathTree.createPathTree(new Factory() {
			@Override
			public Map newMap (){
				return new ConcurrentHashMap<>();
			}

			@Override
			public Set newSet (){
				return new CopyOnWriteArraySet();
			}
		});
	}
	
	
	
	private final Factory factory;
	private final Node    rootNode;
	private String        wordSeparator = "/";
	
	
	
	private PathTree (final Factory factory){
		super();
		
		this.factory = factory;
		this.rootNode = new Node(null);
	}
	
	
	
	public void clear (){
		this.rootNode.clear();
	}
	
	public Cache createCache (final int preferredSize){
		return new Cache(preferredSize);
	}
	
	public Set<Binding> register (final String path, final T value){
		final Set<Binding> bindings;
		
		bindings = new HashSet<>();
		
		if (path.equals("**")) {
			final Binding binding;
			
			this.rootNode.data.add(value);
			
			binding = new Binding(this.rootNode, value);
			
			bindings.add(binding);
		} else {
			this.rootNode.compute(path, (node) -> {
				if (node.data.add(value)) {
					bindings.add(new Binding(node, value));
				}
			});
		}
		
		return bindings;
	}
	
	public void select (final String path, final Consumer<T> consumer){
		this.rootNode.data.forEach(consumer);
		
		if (! path.equals("**")) {
			this.rootNode.select(path, (node) -> node.data.forEach(consumer));
		}
	}
	
	public void setWordSeparator (final String wordSeparator){
		this.wordSeparator = wordSeparator;
	}
	
	@Override
	public String toString (){
		return this.rootNode.toString(0);
	}
	
	public void unregister (final Set<Binding> bindings){
		for (final var binding : bindings) {
			binding.node.data.remove(binding.element);
			
			if (binding.node.empty()) {
				binding.node.unlink();
			}
		}
	}
	
	
	
	public final class Binding {
		private final Node node;
		private final T    element;
		
		
		
		private Binding (final Node node, final T element){
			super();
			
			this.node = node;
			this.element = element;
		}
	}
	
	public final class Cache {
		private final Map<String, WeakReference<Node>> cacheMap = factory.newMap();
		private final int                              preferredSize;
		
		
		
		private Cache (final int preferredSize){
			super();
			
			this.preferredSize = preferredSize;
		}
		
		
		
		public boolean clean (){
			boolean                                                changed = false;
			final Iterator<Map.Entry<String, WeakReference<Node>>> it;
			
			it = this.cacheMap.entrySet().iterator();
			
			while (it.hasNext()) {
				final Map.Entry<String, WeakReference<Node>> entry;
				final Node                                   node;
				final WeakReference<Node>                    nodeRef;
				
				entry = it.next();
				
				nodeRef = entry.getValue();
				
				if (null == nodeRef) {
					it.remove();
					
					changed = true;
					
					continue;
				}
				
				node = nodeRef.get();
				
				if ((null == node) || node.isUnlinked()) {
					it.remove();
					
					changed = true;
				}
			}
			
			return changed;
		}
		
		public void clear (){
			this.cacheMap.clear();
		}
		
		public boolean lookup (final String path, final Consumer<T> consumer){
			final Node                node;
			final WeakReference<Node> nodeRef;
			
			nodeRef = cacheMap.get(path);
			
			if (null == nodeRef) {
				return false;
			}
			
			node = nodeRef.get();
			
			if (node.isUnlinked()) {
				return false;
			}
			
			for (final var element : node.data) {
				consumer.accept(element);
			}
			
			return true;
		}
		
		public Set<Binding> registerThrough (final String path, final T value){
			final Set<Binding> bindings;
			
			if (path.equals("*") || path.equals("**") || containsWildcard(path)) {
				return PathTree.this.register(path, value);
			} else {
				bindings = new HashSet<>();
				
				PathTree.this.rootNode.compute(path, (node) -> {
					if (node.data.add(value)) {
						bindings.add(new Binding(node, value));
						
						Cache.this.store(path, node);
					}
				});
				
				return bindings;
			}
		}
		
		public void remove (final String path){
			this.cacheMap.remove(path);
		}
		
		public void selectThrough (final String path, final Consumer<T> consumer){
			if (! Cache.this.lookup(path, consumer)) {
				PathTree.this.select(path, consumer);
			}
		}
		
		public int size (){
			return this.preferredSize;
		}
		
		private boolean store (final String path, final Node node){
			if (path.equals("*") || path.equals("**")) {
				return false;
			}
			
			if (containsWildcard(path)) {
				return false;
			}
			
			if (((this.size() + 1) >= this.preferredSize) && !this.clean()) {
				return false;
			}
			
			this.cacheMap.put(path, new WeakReference<>(node));
			
			return true;
		}

		public void unregisterThrough (final Set<Binding> bindings){
			for (final var binding : bindings) {
				binding.node.data.remove(binding.element);
				
				if (binding.node.empty()) {
					binding.node.unlink();
				}
			}
			
			this.clean();
		}
	}
	
	public static interface Factory <T> {
		Map<String, Object> newMap ();
		
		Set<T> newSet ();
	}
	
	private final class Node {
		private final Set<T>            data = factory.newSet();
		private Node                    parent;
		private final Map<String, Node> root = factory.newMap();
		
		
		
		private Node (final Node parent){
			super();
			
			this.parent = parent;
		}
		
		
		
		public void clear (){
			this.root.clear();
			this.data.clear();
		}
		
		public void compute (final String path, final Consumer<Node> consumer){
			final int     delimiter;
			final String  key;
			final boolean last;
			final String  next;
			final Node    value;
			
			delimiter = path.indexOf(wordSeparator);

			if (delimiter == -1) {
				key = path;
				next = null;
				last = true;
			} else {
				key = path.substring(0, delimiter);
				next = path.substring(delimiter + 1);
				last = next.isEmpty();
			}

			value = Node.this.root.computeIfAbsent(key, (p) -> new Node(Node.this));

			if (last) {
				consumer.accept(value);
			} else {
				value.compute(next, consumer);
			}
		}
		
		public boolean empty (){
			return (this.data.isEmpty() && this.root.isEmpty());
		}
		
		public boolean isUnlinked (){
			return (null == this.parent);
		}
		
		public boolean remove (final Node node){
			final Iterator<Node> it;
			
			it = this.root.values().iterator();
			
			while (it.hasNext()) {
				final Node next;
				
				next = it.next();
				
				if (next == node) {
					it.remove();
					
					return true;
				}
			}
			
			return false;
		}
		
		public void select (final String path, final Consumer<Node> consumer){
			final int     delimiter;
			final String  key;
			final boolean last;
			final String  next;
			
			delimiter = path.indexOf(wordSeparator);

			if (delimiter == -1) {
				key = path;
				next = null;
				last = true;
			} else {
				key = path.substring(0, delimiter);
				next = path.substring(delimiter + 1);
				last = next.isEmpty();
			}
			
			if (key.equals("*")) {
				if (last) {
					for (final Node value : Node.this.root.values()) {
						consumer.accept(value);
					}
				} else {
					for (final Node value : Node.this.root.values()) {
						value.select(next, consumer);
					}
				}
			} else {
				Node value;
				
				value = Node.this.root.get(key);
				
				if (null != value) {
					if (last) {
						consumer.accept(value);
					} else {
						value.select(next, consumer);
					}
				}
				
				value = this.root.get("*");
				
				if (null != value) {
					if (last) {
						consumer.accept(value);
					} else {
						value.select(next, consumer);
					}
				}
			}
		}
		
		@Override
		public String toString (){
			return this.data.toString();
		}
		
		public String toString (final int depth){
			final StringBuilder sb;
			
			sb = new StringBuilder();
			
			if (! this.data.isEmpty()) {
				sb.append(this.data);
			}
			
			sb.append("\n");
			
			for (final var key : Node.this.root.keySet()) {
				final Node node;
				
				node = Node.this.root.get(key);
				
				sb.append("+");
				sb.append("-".repeat(depth));
				sb.append(">");
				sb.append(" ");
				sb.append(key);
				sb.append(" ");
				sb.append(node.toString(depth + 1));
			}
			
			return sb.toString();
		}
		
		public boolean unlink (){
			final Node from;
			
			from = this.parent;
			
			if (null == from) 
				return false;
			
			this.parent = null;
			
			if (! from.remove(Node.this)) {
				return false;
			}
			
			if (from.empty()) {
				from.unlink();
				
				return true;
			}
			
			return true;
		}
	}
}