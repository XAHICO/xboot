/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.pilot;

import com.xahico.boot.reflection.Reflection;
import com.xahico.boot.reflection.ReflectionMethod;
import java.lang.annotation.Annotation;
import java.util.Iterator;
import java.util.Set;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
final class ServiceLookup {
	public static Set<Class<?>> collectServiceClasses (){
		final Iterator<Class<?>> it;
		final Set<Class<?>>      serviceClasses;
		
		serviceClasses = Reflection.collectClassesAnnotatedWith(ServiceType.class);
		
		it = serviceClasses.iterator();
		
		while (it.hasNext()) {
			final Class<?> jclass;
			
			jclass = it.next();
			
			if (! jclass.isAnnotation()) {
				it.remove();
				//throw new Error("detected invalid service class '%s' (not an annotation)".formatted(serviceClass));
			}
		}
		
		return serviceClasses;
	}
	
	public static boolean isServiceClass (final Class<?> jclass){
		return (jclass.isAnnotation() && jclass.isAnnotationPresent(ServiceType.class));
	}
	
	public static ServiceLookup lookup (final Class<?> jclass){
		if (! isServiceClass(jclass)) {
			throw new Error("detected invalid service class '%s'".formatted(jclass));
		}
		
		return new ServiceLookup((Class<? extends Annotation>)jclass);
	}
	
	public static ServiceConstructor lookupConstructor (final Class<?> jclass) throws LookupException {
		final ReflectionMethod           constructor;
		final Set<ReflectionMethod>      constructorList;
		final Iterator<ReflectionMethod> constructorListIt;
		final Reflection<?>              reflection;
		
		reflection = Reflection.of(jclass);
		
		constructorList = reflection.getMethodsAnnotatedWith(ServiceInitializer.class);
		
		if (constructorList.isEmpty()) {
			throw new LookupException("%s does not declare a service constructor (static @%s method)".formatted(jclass, ServiceInitializer.class));
		}
		
		if (constructorList.size() > 1) {
			throw new Error("");
		}
		
		constructorListIt = constructorList.iterator();
		constructor = constructorListIt.next();
		
		if (! constructor.isStatic()) {
			throw new Error("");
		}
		
		return new ServiceConstructor(constructor);
	}
	
	
	
	private final Class<? extends Annotation> serviceClass;
	
	
	
	private ServiceLookup (final Class<? extends Annotation> serviceClass){
		super();
		
		this.serviceClass = serviceClass;
	}
	
	
	
	public Set<Class<?>> collectInstanceClasses (){
		return Reflection.collectClassesAnnotatedWith(this.serviceClass);
	}
	
	@Deprecated
	public int countInstanceClasses (){
		return this.collectInstanceClasses().size();
	}
	
	public ServiceFactory getFactory (){
		return new ServiceFactory(this.getServiceClass(), this.getServiceProviderClass());
	}
	
	public Class<? extends Annotation> getServiceClass (){
		return this.serviceClass;
	}
	
	public Class<? extends ServiceProvider> getServiceProviderClass (){
		return this.getServiceType().value();
	}
	
	public ServiceType getServiceType (){
		return this.serviceClass.getAnnotation(ServiceType.class);
	}
}