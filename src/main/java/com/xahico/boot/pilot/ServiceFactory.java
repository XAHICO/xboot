/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.pilot;

import com.xahico.boot.reflection.ClassFactory;
import com.xahico.boot.reflection.Reflection;
import com.xahico.boot.reflection.ReflectionMethod;
import com.xahico.boot.util.Exceptions;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
final class ServiceFactory {
	private final Class<? extends Annotation>      serviceClass;
	private final Class<? extends ServiceProvider> serviceProviderClass;
	
	
	
	ServiceFactory (final Class<? extends Annotation> serviceClass, final Class<? extends ServiceProvider> serviceProviderClass){
		super();
		
		this.serviceClass = serviceClass;
		this.serviceProviderClass = serviceProviderClass;
	}
	
	
	
	private Class<?>[] collectDependecies (final Class<?> instanceClass){
		if (instanceClass.isAnnotationPresent(Dependencies.class)) 
			return instanceClass.getAnnotation(Dependencies.class).value();
		else {
			return new Class<?>[0];
		}
	}
	
	private ServiceProvider createServiceProvider (final Class<?> instanceClass){
		try {
			final ReflectionMethod           initializer;
			final Class<?>[]                 initializerParameterTypes;
			final Class<?>                   initializerReturnType;
			final Set<ReflectionMethod>      initializers;
			final Iterator<ReflectionMethod> initializersIt;
			final ClassFactory<?>            instanceClassFactory;
			final Annotation                 service;
			final ServiceConstructor         serviceConstructorDefault;
			final ServiceConstructor         serviceConstructorDeclared;
			final ServiceProvider            serviceProvider;
			final Reflection<?>              serviceProviderReflection;
			
			service = this.getService(instanceClass);
			
			instanceClassFactory = ClassFactory.getClassFactory(instanceClass);
			
			serviceProviderReflection = Reflection.of(serviceProviderClass);
			
			initializers = serviceProviderReflection.getMethodsAnnotatedWith(ServiceFactorizer.class);
			
			if (initializers.isEmpty()) 
				throw new Error("%s: no initializer detected for %s".formatted(Boot.class.getName(), serviceProviderClass));
			else if (initializers.size() > 1) {
				throw new Error("%s: too many initializers detected for %s (expected 1, was %d)".formatted(Boot.class.getName(), serviceProviderClass, initializers.size()));
			}
			
			initializersIt = initializers.iterator();
			initializer = initializersIt.next();
			
			if (! initializer.isStatic()) {
				throw new Error("%s: invalid initializer for %s: not declared static".formatted(Boot.class.getName(), serviceProviderClass));
			}
			
			initializerReturnType = initializer.getReturnClass();
			
			if (initializerReturnType != serviceProviderClass) {
				throw new Error("%s: invalid initializer return type for %s: expected %s, was %s".formatted(Boot.class.getName(), serviceProviderClass, serviceProviderClass, initializerReturnType));
			}
			
			initializerParameterTypes = initializer.getParameterClasses();
			
			if (initializerParameterTypes.length != 2) {
				throw new Error("%s: invalid initializer parameter count for %s: expected 2, was %d".formatted(Boot.class.getName(), serviceProviderClass, initializerParameterTypes.length));
			}
			
			if ((initializerParameterTypes[0] != serviceClass) || (initializerParameterTypes[1] != ClassFactory.class)) {
				throw new Error("%s: invalid initializer parameter types for %s: expected [%s, %s], was %s".formatted(Boot.class.getName(), serviceProviderClass, serviceClass, ClassFactory.class, Arrays.asList(initializerParameterTypes)));
			}
			
			serviceProvider = (ServiceProvider) initializer.invoke(service, instanceClassFactory);
			
			if (null == serviceProvider) {
				throw new Error("%s: initializer of %s returned null: no %s was created (required)".formatted(Boot.class.getName(), serviceProviderClass, serviceProviderClass.getName()));
			}
			
			try {
				serviceConstructorDefault = ServiceLookup.lookupConstructor(serviceProviderClass);
				serviceConstructorDefault.invoke(service, serviceProvider);
			} catch (final LookupException ex) {
				Exceptions.ignore(ex); // instance nor SP declares constructor
			}
			
			try {
				serviceConstructorDeclared = ServiceLookup.lookupConstructor(instanceClass);
				serviceConstructorDeclared.invoke(service, serviceProvider);
			} catch (final LookupException ex) {
				Exceptions.ignore(ex); // instance nor SP declares constructor
			}
			
			return serviceProvider;
		} catch (final ExecutionException ex) {
			throw new Error(ex);
		}
	}
	
	private Annotation getService (final Class<?> instanceClass){
		return instanceClass.getAnnotation(serviceClass);
	}
	
	public Service newInstance (final Class<?> instanceClass){
		return new Service(instanceClass, this.createServiceProvider(instanceClass), this.collectDependecies(instanceClass));
	}
}