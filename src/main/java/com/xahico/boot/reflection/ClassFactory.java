/*
 * Written by Tuomas Kontiainen <https://www.github.com/tutomiko>
 * 
 * Copyright (c) 2023, Tuomas Kontiainen
 */
package com.xahico.boot.reflection;

import com.xahico.boot.synchronicity.Synchronizable;
import com.xahico.boot.synchronicity.Synchronized;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;

/**
 * TBD.
 * 
 * @param <T> 
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public abstract class ClassFactory <T extends Object> {
	public static <T extends Object> ClassFactory<T> getClassFactory (final Class<T> jclass){
		if (implementsUniqueFeatures(jclass)) 
			return getClassFactoryWithFeatures(jclass);
		else {
			return getClassFactoryWithoutFeatures(jclass);
		}
	}
	
	private static <T extends Object> ClassFactory<T> getClassFactoryWithFeatures (final Class<T> jclass){
		try {
			final Class<T>     proxyClass;
			final ProxyFactory proxyFactory;
			
			proxyFactory = new ProxyFactory();
			proxyFactory.setSuperclass(jclass);
			proxyFactory.setFilter(method -> true);
			
			proxyClass = (Class<T>) proxyFactory.createClass(MethodHandles.privateLookupIn(jclass, MethodHandles.lookup()));
			
			return new ClassFactory<>(jclass) {
				@Override
				public T newInstance (){
					try {
						final ProxyObject proxy;
						
						proxy = (ProxyObject) proxyClass.newInstance();
						
						proxy.setHandler(new RichObject());
						
						return (T) proxy;
					} catch (final IllegalAccessException | IllegalArgumentException | InstantiationException ex) {
						throw new Error(ex);
					}
				}
			};
		} catch (final IllegalAccessException | SecurityException ex) {
			throw new Error(ex);
		}
	}
	
	private static <T extends Object> ClassFactory<T> getClassFactoryWithoutFeatures (final Class<T> jclass){
		try {
			final Constructor<T> constructor;
			
			constructor = (Constructor<T>) jclass.getDeclaredConstructor();
			constructor.setAccessible(true);
			
			return new ClassFactory<>(jclass) {
				@Override
				public T newInstance (){
					try {
						return (T) constructor.newInstance();
					} catch (final IllegalAccessException | IllegalArgumentException | InstantiationException | InvocationTargetException ex) {
						throw new Error(ex);
					}
				}
			};
		} catch (final NoSuchMethodException | SecurityException ex) {
			throw new Error(ex);
		}
	}
	
	public static boolean implementsUniqueFeatures (final Class<?> jclass){
		if (Synchronizable.class.isAssignableFrom(jclass)) 
			return true;
		
		return false;
	}
	
	
	
	private final Class<T> jclass;
	
	
	
	private ClassFactory (final Class<T> jclass){
		super();
		
		this.jclass = jclass;
	}
	
	
	
	public final Class<T> getProductionClass (){
		return jclass;
	}
	
	public abstract T newInstance ();
	
	
	
	private static final class RichObject implements MethodHandler {
		@Override
		public Object invoke (final Object proxy, final Method method, final Method proceed, final Object[] args) throws Throwable {
			if (! method.canAccess(proxy))
				method.setAccessible(true);
			
			if (method.isAnnotationPresent(Synchronized.class)) {
				if (proxy instanceof Synchronizable) 
					return this.invokeSync(proxy, method, proceed, args);
				else {
					throw new Error("method '%s' is declared %s while the declaring class [%s] does not implement %s".formatted(method.getName(), Synchronized.class, proxy.getClass().getName(), Synchronizable.class));
				}
			} else {
				return this.invokeAsync(proxy, method, proceed, args);
			}
		}
		
		public Object invokeAsync (final Object proxy, final Method method, final Method proceed, final Object[] args) throws Throwable {
//			System.out.println("ASYNC invocation of %s.%s through ClassFactory instance".formatted(instance, method.getName()));
			
			return proceed.invoke(proxy, args);
		}
		
		public Object invokeSync (final Object proxy, final Method method, final Method proceed, final Object[] args) throws Throwable {
			final Future<?>      future;
			final Synchronizable master;
			
//			System.out.println("SYNC invocation of %s.%s through ClassFactory instance".formatted(instance, method.getName()));
			
			master = (Synchronizable)(proxy);
			
			future = master.call(() -> proceed.invoke(proxy, args));
			
			try {
				return future.get();
			} catch (final ExecutionException ex) {
				throw ex.getCause();
			}
		}
	}
}