/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.publish;

import com.xahico.boot.publish.collections.GWXListBackedNodeCollection;
import com.xahico.boot.publish.collections.GWXListBackedReadOnlyNodeCollection;
import com.xahico.boot.util.OrderedConsumer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Multi-layered path resolver for GWXObjects.
 * 
 * This class supports traversal, lookup and resolution of paths across 
 * twin node trees through "shadow resolution" - 
 * the node trees act as shadow planes to one another and may complement 
 * each other in the resolution of paths that would otherwise be inaccessible 
 * due to flotation.
 * 
 * <p>
 * For example:
 * <pre>
 * Tree A (Global Plane)
 * /networks/1
 *  ^        ^
 *  G        G
 * </pre>
 * <pre>
 * Tree B (Local Plane)
 * /networks/active
 *  ^        ^     
 *  G        L     
 * </pre>
 * 
 * The [/active] node would be inaccessible without Tree A 
 * because the [/networks] node lives on Tree A 
 * and does not exist (except as but a shadow) on Tree B
 * <p>
 * 
 * Through shadow resolution these trees may form complex co-dependent paths 
 * together.
 * 
 * @author Tuomas Kontiainen
 */
public final class GWXPathResolver {
	private final GWXNodeTree fallback;
	private final GWXNodeTree maintail;
	
	
	
	GWXPathResolver (final GWXNodeTree maintail){
		this(maintail, null);
	}
	
	GWXPathResolver (final GWXNodeTree maintail, final GWXNodeTree fallback){
		super();
		
		this.maintail = maintail;
		this.fallback = fallback;
	}
	
	
	
	public GWXObject absolute (final GWXPath path){
		GWXObject resolved;
		
		resolved = this.maintail.lookupRoot(path);
		
		if ((null == resolved) && (null != fallback)) {
			resolved = this.fallback.lookupRoot(path);
		}
		
		return resolved;
	}
	
	public GWXObject absolute (final String path){
		return this.absolute(GWXPath.create(path));
	}
	
	public GWXObject lookup (final GWXPath path){
		final AtomicReference<GWXObject> atom;
		final GWXObject                  resolved;
		final String                     spath;
		
		resolved = this.absolute(path);
		
		if (null != resolved) {
			return resolved;
		}
		
		atom = new AtomicReference(null);
		
		spath = path.toString();
		
		this.traverse(path, (cpath, next) -> {
			if (GWXPath.compare(cpath, spath)) {
				atom.set(next);
			}
			
			return true;
		});
		
		return atom.get();
	}
	
	public GWXObject lookup (final String path){
		return this.lookup(GWXPath.create(path));
	}
	
	public GWXObject[] resolve (final GWXPath path){
		final GWXObject[] resolvePath;

		resolvePath = new GWXObject[path.count()];
		
		this.traverse(path, new OrderedConsumer<>() {
			int index = -1;
			
			@Override
			public boolean accept (final GWXObject resolved){
				index++;
				
				//System.out.println("[%d] = %s".formatted(index, resolved));
				
				resolvePath[index] = resolved;
				
				return true;
			}
		});
		
		//System.out.println("#".repeat(100));
		
		return resolvePath;
	}
	
	public GWXObject[] resolve (final String path){
		return this.resolve(GWXPath.create(path));
	}
	
	public void select (final GWXPath path, final GWXSelector<GWXObject> consumer){
		final AtomicBoolean atom;
		
		atom = new AtomicBoolean(true);
		
		this.select(this.maintail.rootNode(), path, (__, next) -> {
			atom.set(false);
			
			return consumer.call(__, next);
		});
		
		if (atom.get() == true) {
			this.select(this.fallback.rootNode(), path, consumer);
		}
	}
	
	public void select (final String path, final GWXSelector<GWXObject> consumer){
		this.select(GWXPath.create(path), consumer);
	}
	
	public void select (final GWXObject resolveFrom, final GWXPath path, final GWXSelector<GWXObject> consumer){
		this.select(resolveFrom, path.toString(), consumer);
	}
	
	public void select (final GWXObject resolveFrom, final String path, final GWXSelector<GWXObject> consumer){
		this.select(resolveFrom, path, consumer, new HashSet<>());
	}
	
	public void select (final GWXObject resolveFrom, final String path, final GWXSelector<GWXObject> consumer, final Set<GWXObject> visited){
		resolveFrom.select(path, "", (resolvedPath, resolved) -> {
			if (visited.contains(resolved)) {
				return false;
			}
			
			visited.add(resolved);
			
			return consumer.call(resolvedPath, resolved);
		}, (fail) -> {
			final GWXObject resolved;
			
			resolved = this.absolute(fail);
			
			if (null != resolved) {
				if (consumer.call(fail, resolved)) {
					this.select(resolved, fail, consumer, visited);
				}
			}
		});
	}
	
	private GWXNodeCollection shadow (){
		final List<GWXObject> collection;
		
		collection = new ArrayList<>();
		
		return new GWXListBackedReadOnlyNodeCollection(collection);
	}
	
	public void traverse (final GWXPath path, final OrderedConsumer<GWXObject> consumer){
		this.traverse(path, (__, object) -> consumer.accept(object));
	}
	
	public void traverse (final GWXPath path, final GWXPathWalker consumer){
		GWXObject resolved;
		GWXObject resolveFrom;
		String    resolvePath;
		
		resolved = this.absolute(path.root());

		if (null == resolved) {
			return;
		}
		
		resolvePath = path.root();
		
		if (! consumer.accept(resolvePath, resolved)) {
			return;
		}
		
		resolveFrom = resolved;
		
		for (var i = 1; i < path.count(); i++) {
			final String name;
			Object       next;
			
			name = path.get(i);
			
			//System.out.println("try getProperty(%s) of %s".formatted(name, resolveFrom));
			next = resolveFrom.getProperty(name);
			
			if (((null == next) || (!(next instanceof GWXObject))) && (resolveFrom instanceof GWXNodeCollection resolveFromNodes)) {
				//System.out.println("try lookup that shit?");
				next = resolveFromNodes.lookup(name);
				//System.out.println("Lookup for (%s) was ".formatted(name) + next);
			}
			
			if ((null != next) && (next instanceof GWXObject)) {
				resolved = (GWXObject)(next);
			} else {
				resolved = this.absolute(path.toString(i));
				
				if (null == resolved) {
					break;
				}
			}
			
			resolvePath += "/";
			resolvePath += name;
			
			if (! consumer.accept(resolvePath, resolved)) {
				break;
			}
			
			resolveFrom = resolved;
		}
	}
	
	public void traverse (final String path, final OrderedConsumer<GWXObject> consumer){
		this.traverse(GWXPath.create(path), consumer);
	}
}