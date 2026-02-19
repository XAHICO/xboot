/*
 * Written by Tuomas Kontiainen <https://www.github.com/tutomiko>
 * 
 * Copyright (c) 2023, Tuomas Kontiainen
 */
package com.xahico.boot.lang.jsox;

import com.xahico.boot.reflection.Reflection;
import com.xahico.boot.reflection.ReflectionMethod;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javassist.Modifier;
import org.json.JSONObject;

/**
 * TBD.
 * 
 * @param <T> 
 * TBD.
 * 
 * @author Tuomas Kontiainenn
**/
final class JSOXFactory <T extends JSOXObject> {
	private static final Lookup PRIVILEGED_LOOKUP = MethodHandles.lookup();
	
	private static final Map<Class<? extends JSOXObject>, JSOXFactory<?>> instances = new HashMap<>();
	
	
	
	static {
		final Set<Class<? extends JSOXObject>> collection;
		
		collection = Reflection.collectSubclassesOf(JSOXObject.class);
		
		for (final var jclass : collection) {
			final ReflectionMethod                 factorizeMethod;
			final Set<ReflectionMethod>            factorizeMethods;
			final JSOXFactory                      factory;
			final Reflection<? extends JSOXObject> reflection;
			
			reflection = Reflection.of(jclass);
			
			factorizeMethods = reflection.getMethodsAnnotatedWith(JSOXFactorize.class, false);
			
			if (factorizeMethods.size() > 1) 
				throw new Error("%s: multiple %s methods found: %d total: only 1 allowed".formatted(jclass, JSOXFactorize.class.getSimpleName(), factorizeMethods.size()));
			
			if (! factorizeMethods.isEmpty()) {
				factorizeMethod = factorizeMethods.iterator().next();
				
				if (! factorizeMethod.isStatic()) {
					throw new Error("%s: invalid %s method: not static".formatted(jclass, JSOXFactorize.class.getSimpleName()));
				}
				
				if (! factorizeMethod.acceptsParameters(JSOXVariant.class)) {
					throw new Error("%s: invalid %s method: must accept %s".formatted(jclass, JSOXFactorize.class.getSimpleName(), JSOXVariant.class));
				}
				
				if (! Class.class.isAssignableFrom(factorizeMethod.getReturnClass())) {
					throw new Error("%s: invalid %s method: must return instance class".formatted(jclass, JSOXFactorize.class.getSimpleName()));
				}
			} else {
				factorizeMethod = null;
			}
			
			if (Modifier.isAbstract(jclass.getModifiers()) && (null == factorizeMethod))
				continue;
			
			factory = newJSOXFactory(jclass, factorizeMethod);
			
			instances.put(jclass, factory);
		}
	}
	
	
	
	private static void collectSerializableFields (final Class<?> jclass, final Set<JSOXColumn> collection){
		final Reflection<?> reflection;

		if (JSOXObject.class.isAssignableFrom(jclass.getSuperclass()) && (jclass.getSuperclass() != JSOXObject.class)) {
			collectSerializableFields(jclass.getSuperclass(), collection);
		}

		reflection = Reflection.of(jclass);

		for (final var field : reflection.getDeclaredFields()) {
			final JSOXColumn           column;
			final Iterator<JSOXColumn> it;
			final String               key;

			if (field.isAnnotationPresent(JSOXTransient.class))
				continue;

			if (field.isStatic()) 
				continue;

			key = field.getName();

			it = collection.iterator();

			while (it.hasNext()) {
				final JSOXColumn collectColumn;

				collectColumn = it.next();

				if (collectColumn.key().equalsIgnoreCase(key)) {
					it.remove();
				}
			}

			column = new JSOXColumn(field, key);

			collection.add(column);
		}
	}
	
	public static <T extends JSOXObject> JSOXFactory<T> getJSOXFactory (final Class<T> jclass){
		final JSOXFactory<T> factory;
		
		factory = (JSOXFactory<T>) instances.get(jclass);
		
		return factory;
	}
	
	private static <T extends JSOXObject> JSOXFactory<T> newJSOXFactory (final Class<T> jclass, final ReflectionMethod factorizeMethod){
		try {
			final Set<JSOXColumn> columns;
			final Constructor<T>  defaultConstructor;
			final MethodHandle    defaultConstructorHandle;
			final Lookup          lookup;
			
			lookup = MethodHandles.privateLookupIn(jclass, PRIVILEGED_LOOKUP);
			
			if (null != factorizeMethod) {
				defaultConstructor = null;
				defaultConstructorHandle = null;
			} else {
				defaultConstructor = jclass.getDeclaredConstructor();
				defaultConstructorHandle = lookup.unreflectConstructor(defaultConstructor);
			}
			
			columns = new HashSet<>();
			
			collectSerializableFields(jclass, columns);
			
			return new JSOXFactory<>(lookup, defaultConstructorHandle, columns, factorizeMethod);
		} catch (final IllegalAccessException | NoSuchMethodException | SecurityException ex) {
			throw new InternalError(ex);
		}
	}
	
	
	
	private final Set<JSOXColumn>  columns;
	private final MethodHandle     defaultConstructorHandle;
	private final ReflectionMethod factorizeMethod;
	private final Lookup           lookup;
	
	
	
	private JSOXFactory (final Lookup lookup, final MethodHandle defaultConstructorHandle, final Set<JSOXColumn> columns, final ReflectionMethod factorizeMethod){
		super();
		
		this.columns = columns;
		this.lookup = lookup;
		this.defaultConstructorHandle = defaultConstructorHandle;
		this.factorizeMethod = factorizeMethod;
	}
	
	
	
	JSOXColumn column (final String key){
		for (final var column : this.columns) {
			if (column.key().equalsIgnoreCase(key)) {
				return column;
			}
		}
		
		return null;
	}
	
	Set<JSOXColumn> columns (){
		return this.columns;
	}
	
	public Class<? extends JSOXObject> getProductionClass (){
		for (final var jclass : instances.keySet()) {
			if (instances.get(jclass) == JSOXFactory.this) {
				return jclass;
			}
		}
		
		throw new InternalError();
	}
	
	public boolean has (final String key){
		for (final var column : this.columns) {
			if (column.key().equals(key)) {
				return true;
			}
		}
		
		return false;
	}
	
	public T newInstance (){
		return this.newInstance(new JSOXVariant());
	}

	public T newInstance (final JSONObject json){
		return this.newInstance(new JSOXVariant(json));
	}
	
	public T newInstance (final JSOXVariant jsox){
		try {
			final T instance;
			
			if (null != this.factorizeMethod) {
				final Class<T> jclass;
				
				jclass = (Class<T>) this.factorizeMethod.invoke(jsox);
				
				if (null == jclass) {
					return null;
				}
				
				return JSOXFactory.getJSOXFactory(jclass).newInstance(jsox);
			} else {
				instance = (T) this.defaultConstructorHandle.invoke();
				instance.assume(jsox);
			}
			
			return instance;
		} catch (final Throwable ex) {
			throw new Error(jsox.toString(), ex);
		}
	}
}