/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.publish;

import com.xahico.boot.reflection.Reflection;
import com.xahico.boot.reflection.ReflectionField;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
abstract class GWXProperties implements Iterable<String> {
	private static final Map<Class<?>, GWXProperties> CLASS_PROPS = new HashMap<>();
	
	
	
	static {
		for (final var objectClass : Reflection.collectSubclassesOf(GWXObject.class)) {
			final GWXProperties objectProps;
			final GWXResource   resource;
			
			if (! objectClass.isAnnotationPresent(GWXResource.class)) 
				continue;
			
			if (Modifier.isAbstract(objectClass.getModifiers())) 
				continue;
			
			resource = objectClass.getAnnotation(GWXResource.class);
			
			objectProps = createProperties(objectClass, resource);
			
			CLASS_PROPS.put(objectClass, objectProps);
		}
	}
	
	
	
	public static GWXProperties createReflection (final Object id, final Map<String, ?> idol){
		return new GWXProperties() {
			@Override
			public Object get (final Object instance, final String key){
				return idol.get(key);
			}
			
			@Override
			public Object id (final Object instance){
				return id;
			}
			
			@Override
			public Iterator<String> iterator (){
				return idol.keySet().iterator();
			}
		};
	}
	
	private static GWXProperties createProperties (final Class<?> objectClass, final GWXResource resource){
		final Map<String, ReflectionField> fields;
		final AtomicReference<String>      kfield;
		final Reflection<?>                reflection;
		
		kfield = new AtomicReference<>(null);
		
		if ((null != resource) && !resource.root().isBlank()) {
			kfield.set(resource.root());
		}
		
		fields = new HashMap<>();
		
		reflection = Reflection.of(objectClass);
		
		for (final var field : reflection.getDeclaredFields()) {
			final GWXProperty prop;
			final String      propName;
			
			if (! field.isAnnotationPresent(GWXProperty.class)) {
				continue;
			}
			
			prop = field.getAnnotation(GWXProperty.class);
			
			propName = (prop.key().equals("") ? field.getName() : prop.key());
			
			fields.put(propName, field);
			
			if (prop.id()) {
				if (null != kfield.get()) {
					throw new Error("class %s has more than one %s(id = true) annotated fields".formatted(objectClass.getName(), GWXProperty.class.getName()));
				}
				
				kfield.set(propName);
			}
		}
		
		if (null == kfield.get()) {
			//throw new Error("class %s does not have a %s(id = true) annotated field".formatted(objectClass.getName(), GWXProperty.class.getName()));
		}
		
		return new GWXProperties() {
			@Override
			public Object get (final Object instance, final String key){
				final ReflectionField field;
				final Object          object;
				
				field = fields.get(key);
				
				if (null == field) 
					return null;
				
				object = field.get(instance);
				
				if (object instanceof GWXPropertyAccessor accessor) 
					return accessor.get();
				else {
					return object;
				}
			}

			@Override
			public Object id (final Object instance){
				return this.get(instance, kfield.get());
			}

			@Override
			public Iterator<String> iterator (){
				return fields.keySet().iterator();
			}
		};
	}
	
	public static GWXProperties getProperties (final Class<?> objectClass){
		return CLASS_PROPS.get(objectClass);
	}
	
	
	
	public GWXProperties (){
		super();
	}
	
	
	
	public abstract Object get (final Object instance, final String key);
	
	public abstract Object id (final Object instance);
}