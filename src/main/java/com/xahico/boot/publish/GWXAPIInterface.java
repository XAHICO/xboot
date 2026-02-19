/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.publish;

import com.xahico.boot.lang.jsox.JSOXMap;
import com.xahico.boot.lang.jsox.JSOXVariant;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.python.core.Py;
import org.python.core.PyException;
import org.python.core.PyList;
import org.python.core.PyObject;
import org.python.util.PythonInterpreter;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class GWXAPIInterface {
	private static final String STUB_COMPLEX = "IAPI.py";
	
	
	
	public final long                  build;
	private final Callbacks            callbacks;
	private final List<EventProcessor> events = new ArrayList<>();
	private final File                 file;
	private final PythonInterpreter    interpreter = new PythonInterpreter();
	private final AtomicBoolean        loaded = new AtomicBoolean(false);
	private final List<Method>         methods = new ArrayList<>();
	private final AtomicInteger        openHandles = new AtomicInteger(0);
	private final GWXResourceManager   resourceManager;
	private volatile boolean           valid = true;
	public final double                version;
	
	
	
	GWXAPIInterface (final GWXResourceManager resourceManager, final File file, final double version, final Callbacks callbacks){
		super();
		
		this.resourceManager = resourceManager;
		this.file = file;
		this.build = file.lastModified();
		this.callbacks = callbacks;
		this.version = version;
	}
	
	
	
	boolean canDiscard (){
		return (this.openHandles.get() == 0);
	}
	
	boolean canRequire (){
		return (this.valid == true);
	}
	
	boolean destroy (){
		if (! this.valid) 
			return false;
		else {
			this.valid = false;
			
			return true;
		}
	}
	
	public List<EventProcessor> getEventProcessors (){
		return this.events;
	}
	
	public List<Method> getMethods (){
		return this.methods;
	}
	
	public int getOpenHandleCount (){
		return this.openHandles.get();
	}
	
	public synchronized Handle require (){
		if (this.valid == false) {
			return null;
		}
		
		if ((this.openHandles.incrementAndGet() == 1) && (this.loaded.get() == false)) try {
			final PyObject events;
			final PyList   eventsList;
			final PyObject routes;
			final PyList   routesList;
			
			this.loaded.set(true);
			
			interpreter.exec(GWXResourceLoader.loadResource(STUB_COMPLEX));
			
			interpreter.execfile(file.getAbsolutePath());
			
			this.events.clear();
			
			events = interpreter.get("_events");
			eventsList = (PyList) events;
			
			for (final var eventObj : eventsList) {
				final EventProcessor method;
				final PyObject       route;
				final String         routeDoc;
				final PyObject       routeFunction;
				final String         routeId;
				final String         routePath;
				
				route = ((PyObject) eventObj);
				
				routeDoc = route.__getattr__("doc").toString();
				routeFunction = route.__getattr__("func");
				routeId = route.__getattr__("id").toString();
				routePath = route.__getattr__("path").toString();
				
				method = new EventProcessor(routeFunction, routePath, routeId, routeDoc);
				
				this.events.add(method);
			}
			
			this.methods.clear();
			
			routes = interpreter.get("_calls");
			routesList = (PyList) routes;

			for (final var routeObj : routesList) {
				final Method          method;
				final PyObject        route;
				final boolean         routeAsync;
				final boolean         routeAuthorized;
				final String          routeDoc;
				final PyObject        routeFunction;
				final GWXPermission   routeMethod;
				final String          routePath;
				final GWXPermission[] routeRequire;
				
				route = ((PyObject) routeObj);
				
				routeAsync = (boolean) route.__getattr__("async").__tojava__(Boolean.class);
				routeAuthorized = (boolean) route.__getattr__("authorized").__tojava__(Boolean.class);
				routeMethod = GWXPermission.parseSingleString(route.__getattr__("method").toString());
				routePath = route.__getattr__("path").toString();
				routeDoc = route.__getattr__("doc").toString();
				routeFunction = route.__getattr__("func");
				routeRequire = GWXPermission.parsePathString(route.__getattr__("func").toString());
				
				method = new Method(routeFunction, routeMethod, routePath, routeDoc, routeAsync, routeRequire, routeAuthorized);
				
				this.methods.add(method);
			}
		} finally {
			callbacks.onRequire(GWXAPIInterface.this);
		}
		
		return new Handle();
	}
	
	
	
	public static interface Callbacks {
		void onRequire (final GWXAPIInterface iface);
		
		void onRelease (final GWXAPIInterface iface);
	}
	
	public static final class EventProcessor {
		public final String          doc;
		private final PyObject       function;
		public final String          id;
		public final String          path;
		public final GWXPath.Pattern pattern;
		
		
		
		private EventProcessor (final PyObject function, final String path, final String id, final String doc){
			super();
			
			this.function = function;
			this.path = path;
			this.id = id;
			this.doc = doc;
			this.pattern = GWXPath.create(path + "." + id).pattern();
		}
		
		
		
		public GWXEventObject call (final GWXContext context, final GWXInstance instance, final GWXEvent event) throws GWXException {
			final GWXEventObject result;
			final PyObject       resultd;
			final JSOXVariant    resultm;
			final Object         resultn;
			
			try {
				resultd = function.__call__(Py.java2py(context), Py.java2py(instance), Py.java2py(event));
			} catch (final PyException ex) {
				ex.printStackTrace();
				
				throw GWXUtilities.translateRCSException(ex);
			}
			
			resultn = resultd.__tojava__(Object.class);
			
			if (resultn instanceof JSOXVariant) 
				resultm = (JSOXVariant)(resultn);
			else if (resultn instanceof Map) 
				resultm = JSOXMap.wrap((Map)resultn).toVariant();
			else {
				return null;
			}
			
			result = new GWXEventObject();
			result.data = resultm;
			result.id = event.id;
			result.source = event.source.path();
			result.target = event.target.path();
			result.timestamp = event.from;
			
			return result;
		}
	}
	
	public static final class ExchangeAdapter {
		private final GWXExchange exchange;
		public final JSOXVariant  request;
		public final JSOXVariant  response;
		
		
		
		private ExchangeAdapter (final GWXExchange exchange){
			super();
			
			this.exchange = exchange;
			this.request = exchange.request;
			this.response = exchange.response;
		}
		
		
		
		public void ready (){
			this.exchange.ready(null, null);
		}
		
		public void ready (final GWXObject results){
			this.exchange.ready(results, null);
		}
		
		public void ready (final GWXException error){
			this.exchange.ready(null, error);
		}
	}
	
	public final class Handle {
		private boolean released = false;
		
		
		
		private Handle (){
			super();
		}
		
		
		
		public long build (){
			return GWXAPIInterface.this.build;
		}
		
		public EventProcessor lookupEventProcessor (final GWXPath path){
			final String eventId;
			
			eventId = path.getExtension();
			
			for (final var eventObject : events) {
				if (! eventObject.id.equals(eventId)) 
					continue;
				
				if (eventObject.pattern.matches(path)) {
					return eventObject;
				}
			}

			return null;
		}

		public Method lookupMethod (final GWXPermission method, final GWXPath path){
			for (final var methodObject : methods) {
				if (methodObject.method != method) {
					continue;
				}
				
				if (methodObject.pattern.matches(path)) {
					return methodObject;
				}
			}

			return null;
		}

		public boolean release (){
			if (this.released) 
				return false;
			else {
				if (GWXAPIInterface.this.openHandles.decrementAndGet() == 0) {
					callbacks.onRelease(GWXAPIInterface.this);
				}
				
				this.released = true;
				
				return true;
			}
		}
		
		public double version (){
			return GWXAPIInterface.this.version;
		}
	}
	
	public static final class Method {
		public final boolean                 async;
		public final boolean                 authorized;
		public final String                  doc;
		private final PyObject               function;
		public final GWXPermission   method;
		public final String                  path;
		public final GWXPath.Pattern         pattern;
		public final GWXPermission[] require;
		
		
		
		private Method (final PyObject function, final GWXPermission method, final String path, final String doc, final boolean async, final GWXPermission[] require, final boolean authorized){
			super();
			
			this.function = function;
			this.method = method;
			this.doc = doc;
			this.path = path;
			this.pattern = GWXPath.create(path).pattern();
			this.async = async;
			this.require = require;
			this.authorized = authorized;
		}
		
		
		
		public GWXObject call (final GWXContext context, final GWXInstance instance, final GWXExchange exchange) throws GWXException {
			try{
			final ExchangeAdapter exchanged;
			final Object          result;
			final PyObject        resultd;
			
			exchanged = new ExchangeAdapter(exchange);
			
			try {
				resultd = function.__call__(Py.java2py(context), Py.java2py(instance), Py.java2py(exchanged));
			} catch (final PyException ex) {
				ex.printStackTrace();
				
				throw GWXUtilities.translateRCSException(ex);
			}
			
			result = resultd.__tojava__(Object.class);
			
			if (!(result instanceof GWXObject)) {
				return null;
			} else {
				return ((GWXObject)result);
			}
			}catch(Throwable t){
				t.printStackTrace();
				return null;
			}
		}
	}
}