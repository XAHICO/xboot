/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.net.sock.model.lobex;

import com.xahico.boot.net.sock.TCPSessionBasedServiceProvider;
import com.xahico.boot.reflection.ClassFactory;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import com.xahico.boot.pilot.ServiceFactorizer;
import com.xahico.boot.pilot.ServiceInitializer;
import com.xahico.boot.reflection.Reflection;
import com.xahico.boot.reflection.ReflectionMethod;
import com.xahico.boot.util.CollectionUtilities;
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
public final class LOBEXServiceProvider <T extends LOBEXSession> extends TCPSessionBasedServiceProvider<T> {
	@ServiceFactorizer
	private static LOBEXServiceProvider createService (final LOBEXService service, final ClassFactory<? extends LOBEXSession> instanceFactory){
		try {
			return new LOBEXServiceProvider(instanceFactory);
		} catch (final IOException ex) {
			throw new Error(ex);
		}
	}
	
	private static Set<LOBEXMethod> getControlMethods (final Class<? extends LOBEXSession> instanceClass){
		final Set<LOBEXMethod> collection;
		final Reflection<?>   reflection;
		
		reflection = Reflection.of(instanceClass);
		
		collection = new HashSet<>();
		
		for (final ReflectionMethod reflectionMethod : reflection.getMethodsAnnotatedWith(LOBEXControl.class, true)) {
			final LOBEXControl annotation;
			final String       control;
			final int          expectParams;
			final LOBEXMethod  exportMethod;
			final Class[]      paramClasses;
			final Class<?>     requestClass;
			final Class<?>     responseClass;
			
			if (reflectionMethod.isStatic())
				throw new Error(String.format("Invalid export method '%s' is static", reflectionMethod.getName()));
			
			annotation = reflectionMethod.getAnnotation(LOBEXControl.class);
			
			control = annotation.value();
			
			expectParams = 2;
			paramClasses = reflectionMethod.getParameterClasses();
			
			if (paramClasses.length != expectParams) 
				throw new Error(String.format("Invalid declaration of method for '%s': invalid parameter count (expected %d, was %d)", reflectionMethod.getName(), expectParams, paramClasses.length));
			
			requestClass = paramClasses[0];
			
			responseClass = paramClasses[1];
			
			exportMethod = new LOBEXMethod(reflectionMethod, control, requestClass, responseClass);
			
			collection.add(exportMethod);
		}
		
		return collection;
	}
	
	@ServiceInitializer
	private static void initializeService (final LOBEXService service, final LOBEXServiceProvider serviceProvider) throws Throwable {
		serviceProvider.setBindPort(service.port());
		serviceProvider.setBufferSize(service.bufferSize());
		serviceProvider.setEventClass(service.eventClass());
		serviceProvider.setSingleton(service.singleton());
	}
	
	
	
	private int                         bufferSize = -1;
	private Class<? extends LOBEXEvent> eventClass = null;
	private boolean                     singleton = false;
	
	private final ClassFactory<T>       instanceFactory;
	private final ServerSocketChannel   listener;
	private final Set<LOBEXMethod>      methods;
	private final Selector              selector;
	
	
	
	public LOBEXServiceProvider (final Class<T> instanceClass) throws IOException {
		this(ClassFactory.getClassFactory(instanceClass));
	}
	
	public LOBEXServiceProvider (final ClassFactory<T> instanceFactory) throws IOException {
		super();
		
		this.instanceFactory = instanceFactory;
		
		this.methods = getControlMethods(this.instanceFactory.getProductionClass());
		
		this.selector = Selector.open();
		
		this.listener = ServerSocketChannel.open();
	}
	
	
	
	@Override
	protected void cleanup (){
		if (null != this.listener) try {
			this.listener.close();
		} catch (final IOException ex) {
			Exceptions.ignore(ex);
		}
	}
	
	private Map<String, Class<? extends LOBEXEvent>> collectEvents (){
		final Map<String, Class<? extends LOBEXEvent>> eventMap;
		
		eventMap = new HashMap<>();
		
		for (final var eventClassMain : Reflection.collectSubclassesOf(this.eventClass)) {
			final LOBEXEventMarker eventMarker;
			
			eventMarker = eventClassMain.getAnnotation(LOBEXEventMarker.class);
			//System.out.println("Event Class: " + eventClassMain);
			eventMap.put(eventMarker.value(), eventClassMain);
		}
		
		return eventMap;
	}
	
	private T createInstance (){
		final T session;
		
		session = this.instanceFactory.newInstance();
		
		return session;
	}
	
	@Override
	public int getPort (){
		try {
			final InetSocketAddress inetAddress;
			
			inetAddress = (InetSocketAddress) this.listener.getLocalAddress();
			
			return inetAddress.getPort();
		} catch (final IOException ex) {
			return -1;
		}
	}
	
	private void handleDisconnect (final T session, final Throwable cause){
		this.getLogger().log(cause, "session (%s) has disconnected".formatted(session));
		
		if (! session.isDisconnected()) {
			session.markDisconnected();
		}
		
		session.onDisconnect();
		session.destroy();
		session.onDestroy();
		
		sessions.remove(session);
	}
	
	@Override
	protected void initialize () throws Throwable {
		this.listener.bind(new InetSocketAddress("127.0.0.1", this.getBindPort()), 0);
		this.listener.configureBlocking(false);
		this.listener.register(selector, SelectionKey.OP_ACCEPT);
		this.listener.setOption(StandardSocketOptions.SO_REUSEADDR, true);
	}
	
	@Override
	public boolean isIdle (){
		return this.sessions.isEmpty();
	}
	
	@Override
	public boolean isStepper (){
		return true;
	}
	
	@Override
	protected void run (){
		try {
			final Iterator<SelectionKey> it;
			
			selector.selectNow();

			it = selector.selectedKeys().iterator();
			
			while (it.hasNext()) {
				final SocketChannel channel;
				final SelectionKey  key;
				final T             session;
				
				if (this.isStopped()) {
					break;
				}
				
				key = it.next();
				
				it.remove();
				
				if (key.channel() instanceof ServerSocketChannel) {
					if (key.isAcceptable()) {
						channel = listener.accept();
						
						if (this.singleton && (this.sessions.size() == 1)) {
							channel.close();
							
							continue;
						}
						
						channel.configureBlocking(false);
						channel.register(selector, (SelectionKey.OP_READ | SelectionKey.OP_WRITE));
						
						session = this.createInstance();
						session.setExecutor(this.getExecutor());
						session.onCreate();
						session.initializeBuffers(this.bufferSize);
						session.initializeChannel(channel);
						session.initializeMethods(this.methods);
						session.initializeEvents(this.collectEvents());
						session.onConnect();
						
						this.getLogger().log("session (%s) created".formatted(session));
						
						sessions.add(session);
					}
				} else {
					session = CollectionUtilities.seek(this.sessions, (__) -> (__.channel() == key.channel()), false);
					
					if (null == session) 
						continue;
					
					try {
						if (key.isReadable()) {
							session.listen();
						}

						if (key.isWritable()) {
							session.dispatch();
						}
					} catch (final IOException ex) {
						handleDisconnect(session, ex);
					}
				}
			}
		} catch (final IOException ex) {
			Exceptions.ignore(ex);
		}
	}
	
	public void setBufferSize (final int bufferSize){
		this.bufferSize = bufferSize;
	}
	
	public void setEventClass (final Class<? extends LOBEXEvent> eventClass){
		this.eventClass = eventClass;
	}
	
	public void setSingleton (final boolean singleton){
		this.singleton = singleton;
	}
}