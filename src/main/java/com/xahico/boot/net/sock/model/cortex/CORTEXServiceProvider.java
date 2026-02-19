/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.net.sock.model.cortex;

import com.xahico.boot.reflection.ClassFactory;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import com.xahico.boot.pilot.ServiceFactorizer;
import com.xahico.boot.pilot.ServiceInitializer;
import com.xahico.boot.pilot.ServiceProvider;
import com.xahico.boot.reflection.Reflection;
import com.xahico.boot.reflection.ReflectionMethod;
import com.xahico.boot.util.Exceptions;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * TBD.
 * 
 * @param <T> 
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class CORTEXServiceProvider <T extends CORTEXSession> extends ServiceProvider {
	@ServiceFactorizer
	private static CORTEXServiceProvider createService (final CORTEXService service, final ClassFactory<? extends CORTEXSession> instanceFactory){
		try {
			return new CORTEXServiceProvider(instanceFactory);
		} catch (final IOException ex) {
			throw new Error(ex);
		}
	}
	
	private static Set<CORTEXMethod> getControlMethods (final Class<? extends CORTEXSession> instanceClass){
		final Set<CORTEXMethod> collection;
		final Reflection<?>   reflection;
		
		reflection = Reflection.of(instanceClass);
		
		collection = new HashSet<>();
		
		for (final ReflectionMethod reflectionMethod : reflection.getMethodsAnnotatedWith(CORTEXControl.class, true)) {
			final CORTEXControl annotation;
			final String       control;
			final int          expectParams;
			final CORTEXMethod  exportMethod;
			final Class[]      paramClasses;
			final Class<?>     requestClass;
			final Class<?>     responseClass;
			
			if (reflectionMethod.isStatic())
				throw new Error(String.format("Invalid export method '%s' is static", reflectionMethod.getName()));
			
			annotation = reflectionMethod.getAnnotation(CORTEXControl.class);
			
			control = annotation.value();
			
			expectParams = 2;
			paramClasses = reflectionMethod.getParameterClasses();
			
			if (paramClasses.length != expectParams) 
				throw new Error(String.format("Invalid declaration of method for '%s': invalid parameter count (expected %d, was %d)", reflectionMethod.getName(), expectParams, paramClasses.length));
			
			requestClass = paramClasses[0];
			
			responseClass = paramClasses[1];
			
			exportMethod = new CORTEXMethod(reflectionMethod, control, requestClass, responseClass);
			
			collection.add(exportMethod);
		}
		
		return collection;
	}
	
	@ServiceInitializer
	private static void initializeService (final CORTEXService service, final CORTEXServiceProvider serviceProvider) throws Throwable {
		serviceProvider.setBufferSize(service.bufferSize());
		serviceProvider.setConnectPort(service.port());
		serviceProvider.setEventClass(service.eventClass());
		serviceProvider.setHost(service.host());
	}
	
	
	
	private int                          bufferSize = -1;
	private Class<? extends CORTEXEvent> eventClass = null;
	private String                       host = null;
	private int                          port = -1;
	
	private final CORTEXSession          instance;
	private final ClassFactory<T>        instanceFactory;
	private SocketChannel                listener = null;
	private final Set<CORTEXMethod>      methods;
	private Selector                     selector = null;
	
	
	
	public CORTEXServiceProvider (final Class<T> instanceClass) throws IOException {
		this(ClassFactory.getClassFactory(instanceClass));
	}
	
	public CORTEXServiceProvider (final ClassFactory<T> instanceFactory) throws IOException {
		super();
		
		this.instanceFactory = instanceFactory;
		
		this.instance = this.instanceFactory.newInstance();
		
		this.methods = getControlMethods(this.instanceFactory.getProductionClass());
		
		this.selector = Selector.open();
	}
	
	
	
	@Override
	protected void cleanup (){
		this.discard();
		
		instance.onDestroy();
	}
	
	private void discard (){
		if (null != listener) try {
			listener.close();
		} catch (final IOException ex) {
			Exceptions.ignore(ex);
		} finally {
			listener = null;
		}
		
		if (null != selector) try {
			selector.close();
		} catch (final IOException ex) {
			Exceptions.ignore(ex);
		} finally {
			selector = null;
		}
	}
	
	private Map<String, Class<? extends CORTEXEvent>> collectEvents (){
		final Map<String, Class<? extends CORTEXEvent>> eventMap;
		
		eventMap = new HashMap<>();
		
		for (final var eventClassMain : Reflection.collectSubclassesOf(this.eventClass)) {
			final CORTEXEventMarker eventMarker;
			
			eventMarker = eventClassMain.getAnnotation(CORTEXEventMarker.class);
			
			eventMap.put(eventMarker.value(), eventClassMain);
		}
		
		return eventMap;
	}
	
	private void handleConnect (){
		instance.markConnected(true);
		instance.markDisconnected(false);
		instance.onConnect();
	}
	
	private void handleDisconnect (final Throwable cause){
		instance.markDisconnected(true);
		instance.onDisconnect();
	}
	
	@Override
	protected void initialize () throws Throwable {
		instance.setExecutor(this.getExecutor());
		instance.initializeBuffers(this.bufferSize);
		instance.initializeMethods(this.methods);
		instance.initializeEvents(this.collectEvents());
		instance.onCreate();
	}
	
	@Override
	public boolean isIdle (){
		return (!this.instance.isConnected() || this.instance.isDisconnected());
	}
	
	@Override
	public boolean isStepper (){
		return true;
	}
	
	@Override
	protected void run (){
		try {
			final Iterator<SelectionKey> it;
			
			if (null == listener) {
				listener = SocketChannel.open();
				listener.configureBlocking(false);
				listener.connect(new InetSocketAddress(this.host, this.port));
				
				selector = Selector.open();
				
				listener.register(selector, (SelectionKey.OP_CONNECT));
			}
			
			selector.selectNow();
			
			it = selector.selectedKeys().iterator();
			
			while (it.hasNext()) {
				final SelectionKey  key;
				
				if (this.isStopped()) {
					break;
				}
				
				key = it.next();
				
				it.remove();
				
				try {
					if (key.isConnectable()) {
						if (listener.finishConnect()) {
							key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
						}
						
						instance.initializeChannel(listener);
						
						handleConnect();
						
						continue;
					}
					
					if (key.isReadable()) {
						instance.listen();
						
						continue;
					}
					
					if (key.isWritable()) {
						instance.dispatch();
						
						continue;
					}
				} catch (final IOException ex) {
					if (instance.isConnected() && !instance.isDisconnected()) {
						handleDisconnect(ex);
					}
					
					this.discard();
				}
			}
		} catch (final IOException ex) {
			if (instance.isConnected() && !instance.isDisconnected()) {
				handleDisconnect(ex);
			}

			this.discard();
		}
	}
	
	public void setBufferSize (final int bufferSize){
		this.bufferSize = bufferSize;
	}
	
	public void setConnectPort (final int port){
		this.port = port;
	}
	
	public void setEventClass (final Class<? extends CORTEXEvent> eventClass){
		this.eventClass = eventClass;
	}
	
	public void setHost (final String host){
		this.host = host;
	}
}