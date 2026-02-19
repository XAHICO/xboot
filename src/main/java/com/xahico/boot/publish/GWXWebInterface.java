/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.publish;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicLong;
import org.python.core.Py;
import org.python.core.PyException;
import org.python.core.PyList;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.util.PythonInterpreter;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class GWXWebInterface {
	private static final String STUB_COMPLEX = GWXResourceLoader.loadResource("IWeb.py");
	
	
	
	private final Set<ArtifactHandler> artifactHandlers = new CopyOnWriteArraySet<>();
	private final AtomicLong           build = new AtomicLong(0);
	private final Runnable             callbackUpdate;
	private final File                 file;
	private PythonInterpreter          interpreter = null;
	private final Set<PathHandler>     pathHandlers = new CopyOnWriteArraySet<>();
	private final Set<Variable>        variables = new CopyOnWriteArraySet<>();
	
	
	
	GWXWebInterface (final File file, final Runnable callbackUpdate){
		super();
		
		this.file = file;
		this.callbackUpdate = callbackUpdate;
	}
	
	
	
	public boolean available (){
		return (this.build.get() != 0);
	}
	
	public boolean existsPathHandler (final GWXPath path){
		final String pathString;
		
		pathString = path.toString();
		
		for (final var handler : pathHandlers) {
			if (handler.path.equals("/") && pathString.equals("/")) {
				return true;
			}
			
			if (handler.pattern.matches(path)) {
				return true;
			}
		}
		
		return false;
	}
	public Set<Variable> getVariables (){
		return this.variables;
	}
	
	public synchronized boolean load (){
		this.interpreter = new PythonInterpreter();
		
		try (final var in = new FileInputStream(this.file)) {
			final PyObject artifacts;
			final PyList   artifactsList;
			final PyObject routes;
			final PyList   routesList;
			final PyObject vars;
			final PyList   varsList;
			
			this.interpreter.exec(GWXResourceLoader.loadResource("IWeb.py"));
			
			this.interpreter.execfile(in);
			
			artifacts = interpreter.get("_artifacts");
			artifactsList = (PyList) artifacts;
			
			for (final var eventObj : artifactsList) {
				final ArtifactHandler handler;
				final PyObject        route;
				final AccessMode      routeAccess;
				final PyObject        routeFunction;
				final String          routePath;
				
				route = ((PyObject) eventObj);
				
				routeAccess = AccessMode.valueOf(route.__getattr__("access").toString());
				routePath = route.__getattr__("path").toString();
				routeFunction = route.__getattr__("func");
				
				handler = new ArtifactHandler(routeFunction, routePath, routeAccess);
				
				this.artifactHandlers.add(handler);
			}
			
			routes = interpreter.get("_routes");
			routesList = (PyList) routes;

			for (final var routeObj : routesList) {
				final PathHandler handler;
				final PyObject    route;
				final AccessMode  routeAccess;
				final PyObject    routeFunction;
				final String      routePath;
				
				route = ((PyObject) routeObj);
				
				routeAccess = AccessMode.valueOf(route.__getattr__("access").toString());
				routePath = route.__getattr__("path").toString();
				routeFunction = route.__getattr__("func");
				
				handler = new PathHandler(routeFunction, routePath, routeAccess);
				
				this.pathHandlers.add(handler);
			}
			
			vars = interpreter.get("_variables");
			varsList = (PyList) vars;

			for (final var variableObj : varsList) {
				final Variable variable;
				final PyObject variabled;
				final String   variableKey;
				final String   variableVal;
				
				variabled = ((PyObject) variableObj);
				
				variableKey = variabled.__getattr__("key").toString();
				variableVal = variabled.__getattr__("value").toString();
				
				variable = new Variable(variableKey, variableVal);
				
				this.variables.add(variable);
			}
			
			this.build.set(file.lastModified());
			
			this.callbackUpdate.run();
			
			return true;
		} catch (final FileNotFoundException ex) {
			return false;
		} catch (final IOException ex) {
			return false;
		}
	}
	
	public ArtifactHandler lookupArtifactHandler (final GWXPath path, final boolean auth){
		for (final var handler : artifactHandlers) {
			if ((auth == true) && (handler.access == AccessMode.PUBLIC)) {
				continue;
			}
			
			if ((auth == false) && (handler.access == AccessMode.PRIVATE)) {
				continue;
			}
			
			if (handler.pattern.matches(path)) {
				return handler;
			}
		}

		return null;
	}
	
	public PathHandler lookupPathHandler (final GWXPath path, final boolean auth){
		final String pathString;
		
		pathString = path.toString();
		
		for (final var handler : pathHandlers) {
			if ((auth == true) && (handler.access == AccessMode.PUBLIC)) {
				continue;
			}
			
			if ((auth == false) && (handler.access == AccessMode.PRIVATE)) {
				continue;
			}
			
			if (handler.path.equals("/") && pathString.equals("/")) {
				return handler;
			}
			
			if (handler.pattern.matches(path)) {
				return handler;
			}
		}

		return null;
	}
	
	public synchronized void unload (){
		if (null != this.interpreter) {
			this.interpreter.close();
			this.interpreter = null;
		}
		
		this.variables.clear();
		
		this.pathHandlers.clear();
		
		this.artifactHandlers.clear();
		
		this.build.set(0);
	}
	
	public boolean update (){
		final long buildLatest;
		
		buildLatest = this.file.lastModified();
		
		if (this.build.get() == buildLatest) {
			return false;
		}
		
		this.unload();
		
		if (! this.load()) {
			this.build.set(0);
			
			return false;
		}
		
		return true;
	}
	
	
	
	public static enum AccessMode {
		PRIVATE,
		PUBLIC,
		SHARED,
	}
	
	public static class ArtifactHandler {
		private final AccessMode     access;
		private final PyObject       function;
		private final String         path;
		public final GWXPath.Pattern pattern;
		
		
		
		private ArtifactHandler (final PyObject function, final String path, final AccessMode access){
			super();
			
			this.function = function;
			this.path = path;
			this.access = access;
			this.pattern = GWXPath.create(path).pattern();
		}
		
		
		
		public String call (final GWXContext context, final GWXInstance instance){
			final PyObject resultd;
			
			try {
				resultd = function.__call__(Py.java2py(context), Py.java2py(instance));
			} catch (final PyException ex) {
				return null;
			}
			
			if (!(resultd instanceof PyString)) {
				return null;
			} else {
				return (String)((PyString)resultd).__tojava__(String.class);
			}
		}
	}
	
	public static class PathHandler {
		private final AccessMode     access;
		private final PyObject       function;
		private final String         path;
		public final GWXPath.Pattern pattern;
		
		
		
		private PathHandler (final PyObject function, final String path, final AccessMode access){
			super();
			
			this.function = function;
			this.path = path;
			this.access = access;
			this.pattern = GWXPath.create(path).pattern();
		}
		
		
		
		public String call (final GWXContext context, final GWXInstance instance){
			final PyObject resultd;
			
			try {
				resultd = function.__call__(Py.java2py(context), Py.java2py(instance));
			} catch (final PyException ex) {
				return null;
			}
			
			if (!(resultd instanceof PyString)) {
				return null;
			} else {
				return (String)((PyString)resultd).__tojava__(String.class);
			}
		}
	}
	
	public static class Variable {
		public final String key;
		public final String value;
		
		
		
		private Variable (final String key, final String value){
			super();
			
			this.key = key;
			this.value = value;
		}
	}
}