/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.pilot;

import com.xahico.boot.reflection.ClassFactory;
import com.xahico.boot.reflection.Reflection;
import com.xahico.boot.reflection.ReflectionMethod;
import com.xahico.boot.util.Parameters;
import com.xahico.boot.util.ParametersParser;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class Boot {
	private static Runnable                onLoaded = () -> {};
	private static final Deque<Launchable> stack = new LinkedList<>();
	
	
	
	public static void launch (final String[] args) throws Error {
		final Iterator<Class<?>>         classIt;
		final Set<Class<?>>              classList;
		final ReflectionMethod           entryPoint;
		final Iterator<ReflectionMethod> entryPointIt;
		final Set<ReflectionMethod>      entryPointList;
		final Object[]                   invokeArgs;
		final BootLoader                 launchables;
		final Class<?>                   mainClass;
		final ClassFactory<?>            mainClassFactory;
		final Object                     mainInstance;
		final Reflection<?>              mainReflection;
		
		System.out.println("Loading " + Core.class);
		
		/*
		 * Initialize main class.
		 */
		classList = Reflection.collectClassesAnnotatedWith(MainClass.class);
		
		if (classList.isEmpty()) 
			System.out.println("%s: no main class detected".formatted(Boot.class.getName()));
		else if (classList.size() > 1) 
			throw new Error("%s: multiple main classes were detected %s".formatted(Boot.class.getName(), classList));
		else try {
			classIt = classList.iterator();
			
			mainClass = classIt.next();
			
			System.out.println("%s: main class detected: %s".formatted(Boot.class.getName(), mainClass.getName()));
			
			mainReflection = Reflection.of(mainClass);
			
			entryPointList = mainReflection.getMethodsAnnotatedWith(MainEntryPoint.class);
			
			if (entryPointList.isEmpty()) {
				throw new Error("%s: no main entry point detected in %s".formatted(Boot.class.getName(), mainClass.getName()));
			}
			
			entryPointIt = entryPointList.iterator();
			
			entryPoint = entryPointIt.next();
			
			mainClassFactory = ClassFactory.getClassFactory(mainClass);
			
			mainInstance = mainClassFactory.newInstance();
			
			switch (entryPoint.getParameterCount()) {
				case 0: {
					if (entryPoint.isStatic()) {
						invokeArgs = new Object[0];
					} else {
						invokeArgs = new Object[]{mainInstance};
					}
					
					break;
				}
				case 1: {
					final Object   argsAdjusted;
					final Class<?> argsClass;
					
					argsClass = entryPoint.getParameterClasses()[0];
					
					if (argsClass == Parameters.class) {
						final Parameters       params;
						final ParametersParser paramsParser;
						
						paramsParser = new ParametersParser();
						paramsParser.setPrefix("-");
						paramsParser.mount(args);
						
						params = paramsParser.parse();
						
						argsAdjusted = params;
					} else if (argsClass == String[].class) {
						argsAdjusted = args;
					} else {
						throw new Error();
					}
					
					if (entryPoint.isStatic()) {
						invokeArgs = new Object[]{argsAdjusted};
					} else {
						invokeArgs = new Object[2];
						invokeArgs[0] = mainInstance;
						invokeArgs[1] = argsAdjusted;
					}
					
					break;
				}
				default: {
					throw new Error("%s: main entry point has %d parameters: must be %s, %s or none".formatted(Boot.class.getName(), entryPoint.getParameterCount(), String[].class, Parameters.class));
				}
			}
			
			entryPoint.invoke(invokeArgs);
		} catch (final ExecutionException ex) {
			throw new Error(ex);
		}
		
		/*
		 * Collect launchables and create a 'Boot Sequence'
		 */
		launchables = new BootLoader();
		
		for (final var autoStartableClass : Reflection.collectClassesAnnotatedWith(AutoStart.class)) {
			final Instance        instance;
			final InstanceFactory instanceFactory;
			
			System.out.println("%s: found @%s class '%s'".formatted(Boot.class.getName(), AutoStart.class.getName(), autoStartableClass.getName()));
			
			instanceFactory = InstanceFactory.getInstanceFactory(autoStartableClass);
			
			instance = instanceFactory.newInstance();
			
			launchables.add(instance);
		}
		
		for (final var serviceClass : ServiceLookup.collectServiceClasses()) {
			final ServiceFactory serviceFactory;
			final ServiceLookup  serviceLookup;
			
			serviceLookup = ServiceLookup.lookup(serviceClass);
			
			if (serviceLookup.countInstanceClasses() == 0) 
				continue;
			
			System.out.println("%s: found %d classes for service type '%s' (%s):".formatted(Boot.class.getName(), serviceLookup.countInstanceClasses(), serviceLookup.getServiceClass(), serviceLookup.getServiceProviderClass().getName()));
			
			serviceFactory = serviceLookup.getFactory();
			
			for (final var instanceClass : serviceLookup.collectInstanceClasses()) {
				final Service service;
				
				service = serviceFactory.newInstance(instanceClass);
				
				System.out.println("\t+ %s".formatted(instanceClass));
				
				Services.register(service);
				
				launchables.add(service);
			}
		}
		
		if (launchables.steps() > 0) {
			System.out.println();
			System.out.println("Collected Boot Sequence:");
			
			launchables.setHandler(launchable -> {
				System.out.println("$> boot %s:".formatted(launchable));
				
				launchable.start();
				
				stack.addLast(launchable);
			});
			
			launchables.load();
			
			System.out.println("%s: startup sequence completed".formatted(Boot.class));
		}
		
		Boot.onLoaded.run();
	}
	
	public static void onLoaded (final Runnable runnable){
		Boot.onLoaded = runnable;
	}
	
	public static void shutdown (){
		for (final var launchable : stack) {
			System.out.println("$> sd %s:".formatted(launchable));
			
			launchable.stop();
		}
		
		System.exit(0);
	}
	
	
	
	private Boot (){
		throw new UnsupportedOperationException("Not supported.");
	}
}