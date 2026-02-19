/*
 * Written by Tuomas Kontiainen <https://www.github.com/tutomiko>
 * 
 * Copyright (c) 2023, Tuomas Kontiainen
 */
package com.xahico.boot.pilot;

import com.xahico.boot.reflection.Reflection;
import java.lang.invoke.MethodHandle;
import java.util.HashMap;
import java.util.Map;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainenn
**/
final class InstanceFactory {
	public static InstanceFactory getInstanceFactory (final Class<?> jclass){
		return new InstanceFactory(jclass);
	}
	
	
	
	private final Class<?>      jclass;
	private final Reflection<?> reflection;
	
	
	
	private InstanceFactory (final Class<?> jclass){
		super();
		
		this.jclass = jclass;
		this.reflection = Reflection.of(jclass);
	}
	
	
	
	public Instance newInstance (){
		final Map<ExportType, MethodHandle> exports;

		exports = new HashMap<>();

		for (final var method : reflection.getMethodsAnnotatedWith(Export.class, true)) {
			final Export       export;
			final ExportType   exportType;
			final MethodHandle methodHandle;

			export = method.getAnnotation(Export.class);

			exportType = export.value();
			
			methodHandle = method.getHandle();

			exports.put(exportType, methodHandle);
		}

		return new Instance(jclass, exports);
	}
}