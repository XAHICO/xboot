/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.publish;

import java.util.HashMap;
import java.util.Map;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class GWXContext {
	static GWXContext buildContext (final GWXResourceManager rcm, final GWXSession session, final GWXPath.Pattern pattern, final GWXPath path, final GWXPermission mode){
		final GWXContext  context;
		final GWXObject[] pathObjects;
		
		//System.out.println("Building Context for %s %s".formatted(mode, path));
		
		if (null == mode) {
			pathObjects = new GWXObject[0];
		} else {
			//System.out.println("Resolving '%s'".formatted(path));
			
			pathObjects = rcm.resolve(session, path);
			
			System.out.println("-".repeat(100));
			System.out.println("RESOLVED '%s' TO:".formatted(path));
			for (final var po : pathObjects) {
				System.out.println(po);
			}
			System.out.println("-".repeat(100));
		}
		
		context = new GWXContext(pattern, path, pathObjects);
		
		for (final var queryKey : pattern.keys()) {
			final int    queryIndex;
			final String queryValue;
			
			queryValue = path.get(queryKey, pattern);
			
			context.query.put(queryKey, queryValue);
			
			queryIndex = pattern.find(queryKey);
			
			if (pathObjects.length > queryIndex) {
				context.queryObjects.put(queryKey, pathObjects[queryIndex]);
			}
		}
		
		return context;
	}
	
	
	
	public final String                  path;
	private final GWXObject[]            pathObjects;
	public final String                  pattern;
	public final Map<String, String>     query = new HashMap<>();
	private final Map<String, GWXObject> queryObjects = new HashMap<>();
	
	
	
	GWXContext (final GWXPath.Pattern pattern, final GWXPath path, final GWXObject[] pathObjects){
		super();
		
		this.pattern = pattern.toString();
		this.path = path.toString();
		this.pathObjects = pathObjects;
	}
	
	
	
	public boolean checkAccess (final GWXSession session, final GWXPermission[] accessPath){
		if (accessPath.length == 1) {
			final GWXObject target;
			
			if (accessPath[0] == null) 
				return true;
			
			target = this.lookup(-1);
			
			if (null == target) 
				return true;
			
			return session.checkAccess(target, accessPath);
		} else {
			for (var i = 0; i < this.pathObjects.length; i++) {
				final GWXPermission access;
				final GWXObject             target;
				
				target = this.pathObjects[i];
				
				if ((i + 1) >= accessPath.length) 
					return true;
				
				access = accessPath[i];
				
				if (access == null) 
					continue;
				
				if (! session.checkAccess(target, access)) {
					return false;
				}
			}
			
			return true;
		}
	}
	
	public GWXObject lookup (final int index){
		if (index == -1) {
			return this.lookup(this.pathObjects.length - 1);
		} else if (index < this.pathObjects.length) {
			return this.pathObjects[index];
		} else {
			return null;
		}
	}
	
	public GWXObject lookup (final String key){
		return this.queryObjects.get(key);
	}
}