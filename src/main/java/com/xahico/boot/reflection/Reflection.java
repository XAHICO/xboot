/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.reflection;

import com.xahico.boot.platform.FileUtilities;
import com.xahico.boot.util.Exceptions;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import sun.misc.Unsafe;

/**
 * TBD.
 * 
 * @param <T> 
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class Reflection <T> {
	private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
	
	
	
	private static final Class[] appClasses;
	
	
	
	static {
		final ClassGraph  graph;
		final Set<String> paths;
		final long        whenb;
		final long        whene;
		
		whenb = System.currentTimeMillis();
		
		graph = new ClassGraph().enableAllInfo();
		
		paths = new HashSet<>();
		
		graph.getClasspathURIs().forEach(uri -> {
			final String classPath;
			
			classPath = uri.toString();
			
			if (classPath.endsWith("/") || classPath.contains("target/classes") || classPath.contains("build/classes") || classPath.matches(".*[/\\\\]myapp-.*\\.jar$")) {
				paths.add(classPath);
				
				try {
					FileUtilities.walkDirectory(new File(uri.getPath()), 2, (file) -> {
						if (file.isDirectory() && !file.getParentFile().getName().equals("classes")) {
							final StringBuilder pathBuilder;
							
							pathBuilder = new StringBuilder();
							pathBuilder.append(file.getParentFile().getName());
							pathBuilder.append(".");
							pathBuilder.append(file.getName());
							
							paths.add(pathBuilder.toString());
						}
					});
				} catch (final ExecutionException | IOException ex) {
					throw new InternalError(ex);
				}
			}
		});
		
		if (! paths.contains("com.xahico.boot")) {
			paths.add("com.xahico.boot");
		}
		
		try (final ScanResult scan = graph.acceptPackages(paths.toArray(new String[0])).enableAnnotationInfo().enableClassInfo().scan()) {
			final List<Class<?>> loaded;
			
			loaded = new ArrayList<>();
			
			for (final var info : scan.getAllClasses()) {
				loaded.add(info.loadClass());   // eager, deterministic
			}
			
			appClasses = loaded.toArray(new Class[0]);
		}
		
		whene = System.currentTimeMillis();
		
		System.out.println("Application Class scan completed in %d millisecond(s)".formatted(whene - whenb));
	}

	public static Set<Class<?>> collectClassesAnnotatedWith (final Class<? extends Annotation> annotationClass){
		final Set<Class<?>> collection;
		
		collection = new HashSet<>();
		
		for (final var c : appClasses) {
			if (c.isAnnotationPresent(annotationClass)) {
				collection.add(c);
			}
		}
		
		return collection;
	}
	
	public static <T> Set<Class<? extends T>> collectSubclassesOf (final Class<T> jclass){
		final Set<Class<? extends T>> collection;
		
		collection = new HashSet<>();
		
		for (final var c : appClasses) {
			if ((c != jclass) && jclass.isAssignableFrom(c)) {
				collection.add(c);
			}
		}
		
		return collection;
	}
	
	public static <T> Reflection<T> of (final Class<T> jclass){
		try {
			final MethodHandles.Lookup lookup;
			
			lookup = MethodHandles.privateLookupIn(jclass, LOOKUP);
			
			return new Reflection<>(jclass, lookup);
		} catch (final IllegalAccessException ex) {
			throw new Error(ex);
		}
	}
	
	
	
	private final Class<T>             jclass;
	private final MethodHandles.Lookup lookup;
	
	
	
	private Reflection (final Class<T> jclass, final MethodHandles.Lookup lookup){
		super();
		
		this.jclass = jclass;
		this.lookup = lookup;
	}
	
	
	
	public Set<ReflectionField> getAllFields (){
		final Set<ReflectionField> collection;
		
		collection = new HashSet<>();
		
		if (this.jclass.getSuperclass() != Object.class) {
			collection.addAll(Reflection.of(this.jclass.getSuperclass()).getAllFields());
		}
		
		collection.addAll(this.getDeclaredFields());
		
		return collection;
	}
	
	public Set<ReflectionField> getDeclaredFields (){
		final Field[]              nativeFieldArray;
		final Set<ReflectionField> superFieldArray;
		
		nativeFieldArray = jclass.getDeclaredFields();
		
		superFieldArray = new HashSet<>();
		
		for (var i = 0; i < nativeFieldArray.length; i++) try {
			superFieldArray.add(openField(nativeFieldArray[i]));
		} catch (final IllegalAccessException ex) {
			Exceptions.ignore(ex);
		}
		
		return superFieldArray;
	}
	
	public List<Enum<?>> getEnumValues (){
		if (! jclass.isEnum()) {
			throw new IllegalArgumentException("%s is not an enum class".formatted(jclass.getName()));
		}
		
		return Arrays.stream(jclass.getEnumConstants())
			       .map(e -> (Enum<?>) e)
			       .collect(Collectors.toList());
	}
	
	public ReflectionField getField (final String fieldName) throws IllegalAccessException, NoSuchFieldException {
		final Field field;
		
		field = jclass.getDeclaredField(fieldName);
		
		return openField(field);
	}
	
	public Set<ReflectionMethod> getMethodsAnnotatedWith (final Class<? extends Annotation> annotationClass){
		return this.getMethodsAnnotatedWith(annotationClass, false);
	}
	
	public Set<ReflectionMethod> getMethodsAnnotatedWith (final Class<? extends Annotation> annotationClass, final boolean traverse){
		final Set<ReflectionMethod> collection;
		
		collection = new HashSet<>();
		
		if (traverse && (jclass.getSuperclass() != Object.class)) {
			collection.addAll(Reflection.of(jclass.getSuperclass()).getMethodsAnnotatedWith(annotationClass, traverse));
		}
		
		for (final Method method : jclass.getDeclaredMethods()) {
			final ReflectionMethod exportMethod;
			final MethodHandle     methodHandle;
			
			if (! method.isAnnotationPresent(annotationClass)) 
				continue;
			
			method.setAccessible(true);
			
			try {
				methodHandle = lookup.unreflect(method);
			} catch (final IllegalAccessException ex) {
				throw new Error(ex);
			}
			
			exportMethod = new ReflectionMethod(method, methodHandle);
			
			collection.add(exportMethod);
		}
		
		return collection;
	}
	
	public T newInstance (){
		try {
			final Constructor<T> ctor;
			
			ctor = this.jclass.getDeclaredConstructor();
			ctor.setAccessible(true);
			
			return ctor.newInstance();
		} catch (final NoSuchMethodException ex) {
			return null;
		} catch (final SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
			throw new Error(this.jclass.toString(), ex);
		}
	}
	
	public T newInstanceOrDefault (){
		T instance;
		
		instance = this.newInstance();
		
		if (null == instance) {
			instance = this.newInstanceWithoutConstructor();
		}
		
		return instance;
	}
	
	public T newInstanceWithoutConstructor (){
		try {
			final Field  field;
			final Unsafe unsafe;
			
			field = Unsafe.class.getDeclaredField("theUnsafe");
			field.setAccessible(true);
			
			unsafe = (Unsafe) field.get(null);
			
			return (T) unsafe.allocateInstance(this.jclass);
		} catch (final IllegalAccessException | IllegalArgumentException | InstantiationException ex) {
			throw new Error(ex);
		} catch (final NoSuchFieldException | SecurityException ex) {
			throw new InternalError(ex);
		}
	}
	
	private ReflectionField openField (final Field field) throws IllegalAccessException {
		final MethodHandle methodHandleGet;
		final MethodHandle methodHandleSet;

		field.setAccessible(true);

		methodHandleGet = lookup.unreflectGetter(field);

		methodHandleSet = lookup.unreflectSetter(field);

		return new ReflectionField(field, methodHandleGet, methodHandleSet);
	}
}