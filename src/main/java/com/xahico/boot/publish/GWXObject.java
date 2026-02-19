/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.publish;

import com.xahico.boot.lang.jsox.JSOXVariant;
import com.xahico.boot.util.Exceptions;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public abstract class GWXObject {
	private volatile Map<String, List<GWXEventHandler>> eventHandlers = null;
	private volatile String                             name = null;
	private GWXObject                                   parent = null;
	private final GWXProperties                         properties;
	
	
	
	GWXObject (){
		super();
		
		this.properties = GWXProperties.getProperties(this.getClass());
	}
	
	GWXObject (final GWXProperties properties){
		super();
		
		this.properties = properties;
	}
	
	
	
	final void addEventHandler (final String eventId, final GWXEventHandler eventHandler){
		Map<String, List<GWXEventHandler>> map = eventHandlers;

		if (map == null) {
			synchronized (this) {
				if (eventHandlers == null) {
					eventHandlers = new ConcurrentHashMap<>();
				}
				
				map = eventHandlers;
			}
		}

		map.computeIfAbsent(eventId, k -> new CopyOnWriteArrayList<>()).add(eventHandler);
	}
	
	final boolean checkKey (final Object key){
		final Object keyOwn;
		
		if (null == key) 
			return false;
		
		keyOwn = this.getId();
		
		if (key == keyOwn) 
			return true;
		
		if (keyOwn instanceof String) 
			return key.toString().equals(keyOwn);
		
		if (key instanceof String) 
			return keyOwn.toString().equals(key);
		
		return key.equals(keyOwn);
	}
	
	protected void cleanup (){
		if (null != this.properties) {
			for (final var propertyKey : this.properties) {
				final Object property;

				property = this.properties.get(this, propertyKey);

				if (null == property) 
					continue;

				if (property instanceof GWXObject propertyObject) {
					propertyObject.cleanup();
				}
			}
		}
	}
	
	final void destroy (){
		this.unlink();
		
		if (null != this.eventHandlers) {
			this.eventHandlers.values().forEach(handlers -> handlers.clear());
			this.eventHandlers.clear();
			this.eventHandlers = null;
		}
		
		this.cleanup();
	}
	
	final void fireEvent (final GWXEvent event){
		final String path;
		
		path = this.path();
		
		if (null != this.eventHandlers) {
			fireEventHandlers(this.eventHandlers.get("**"), path, event);
			
			if (event.source == this) {
				fireEventHandlers(this.eventHandlers.get("*"), path, event);
				
				fireEventHandlers(this.eventHandlers.get(event.id), path, event);
			}
		}
		
		if (null != this.parent) {
			this.parent.fireEvent(event);
		}
	}
	
	private void fireEventHandlers (final List<GWXEventHandler> handlers, final String path, final GWXEvent event){
		if (null != handlers) {
			handlers.forEach((handler) -> {
				try {
					handler.handle(path, event);
				} catch (final Throwable t) {
					Exceptions.ignore(t);
				}
			});
		}
	}
	
	final Object getId (){
		if (null != this.properties) {
			return this.properties.id(this);
		} else {
			return null;
		}
	}
	
	final GWXProperties getProperties (){
		return this.properties;
	}
	
	public final Object getProperty (final String key){
		if (null != this.properties) {
			return this.properties.get(this, key);
		} else {
			return null;
		}
	}
	
	final GWXObject getParent (){
		return this.parent;
	}
		
	final String getPropertyKey (final Object object){
		for (final var propertyKey : this.properties) {
			final Object property;
			
			property = this.properties.get(this, propertyKey);
			
			if (property == object) {
				return propertyKey;
			}
		}
		
		return null;
	}
	
	protected void initialize () throws Throwable {
		
	}
	
	protected void initialize (final GWXSession session) throws Throwable {
		
	}
	
	final void link (final GWXObject parent){
		assert(null == this.parent);
		
		this.parent = parent;
	}
	
	final String name (){
		final Object id;
		
		if (null != this.name) {
			return this.name;
		}
		
		id = this.getId();
		
		if (null != id) {
			return id.toString();
		}
		
		if (null != this.getParent()) {
			final String assigned;
			
			assigned = this.getParent().getPropertyKey(this);
			
			if (null != assigned) {
				this.name = assigned;
			} else {
				this.name = "";
			}
		}
		
		return this.name;
	}
	
	final void name (final String name){
		this.name = name;
	}
	
	final String path (){
		if (null != this.parent) {
			return (this.parent.path() + "/" + this.name());
		} else {
			return ((null != this.name()) ? this.name() : "");
		}
	}
	
	final void removeEventHandler (final String eventId, final GWXEventHandler eventHandler){
		final List<GWXEventHandler> eventHandlerSet;
		
		if (null != this.eventHandlers) {
			eventHandlerSet = this.eventHandlers.get(eventId);
			
			if (null != eventHandlerSet) {
				if (eventHandlerSet.remove(eventHandler) && eventHandlerSet.isEmpty()) {
					this.eventHandlers.remove(eventId);
				}
			}
		}
	}
	
	final GWXObject root (){
		if (null == this.parent) 
			return this;
		else {
			return this.parent;
		}
	}
	
	void select (final String path, final String from, final GWXSelector consumer, final GWXPathResolveFallbackHandler fallback){
		final int     delimiter;
		final String  key;
		final boolean last;
		final String  next;

		delimiter = path.indexOf('/');

		if (delimiter == -1) {
			key = path;
			next = null;
			last = true;
		} else {
			key = path.substring(0, delimiter);
			next = path.substring(delimiter + 1);
			last = next.isEmpty();
		}

		if (key.equals("**") && last) {
			// FIXED: Don't recurse with "**" - it causes infinite loops
			// Instead, just call consumer on this object and direct children
			consumer.call(from, this);

			for (final var propertyKey : this.properties) {
				final Object property;

				property = this.properties.get(this, propertyKey);

				if (property instanceof GWXObject propertyObject) {
					// Call consumer on direct child only - do NOT recurse with "**"
					if (!consumer.call((from + "/" + propertyKey), propertyObject)) {
						break;
					}

					// If property is itself a collection/container, get its direct children too (one level)
					if (propertyObject instanceof GWXNodeCollection) {
						((GWXNodeCollection<?>) propertyObject).walk((child) -> {
							consumer.call((from + "/" + propertyKey + "/" + child.getId()), child);
						});
					}
				}
			}
		} else if (key.equals("*")) {
			if (last) {
				for (final var propertyKey : this.properties) {
					final Object property;

					property = this.properties.get(this, propertyKey);

					if (property instanceof GWXObject) {
						if (! consumer.call((from + "/" + propertyKey), (GWXObject)property)) {
							break;
						}
					}
				}
			} else {
				for (final var propertyKey : this.properties) {
					final Object property;

					property = this.properties.get(this, propertyKey);

					if (property instanceof GWXObject propertyObject) {
						propertyObject.select(next, (from + "/" + propertyKey), consumer, fallback);
					}
				}
			}
		} else {
			final Object property;

			property = this.properties.get(this, key);

			if (null == property) {
				fallback.fallback(path);
			} else if (property instanceof GWXObject propertyObject) {
				if (last) {
					consumer.call((from + "/" + key), propertyObject);
				} else {
					propertyObject.select(next, (from + "/" + key), consumer, fallback);
				}
			}
		}
	}
	
	public final byte[] serialize (){
		return this.serialize(true);
	}
	
	public abstract byte[] serialize (final boolean internal);
	
	public abstract JSOXVariant snapshot ();
	
	final void unlink (){
		//this.parent = null;
	}
}