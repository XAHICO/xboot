/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.net.sock.model.asio;

import com.xahico.boot.io.IOByteBuffer;
import com.xahico.boot.io.IODynamicByteBuffer;
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
import com.xahico.boot.util.ArrayUtilities;
import com.xahico.boot.util.Exceptions;

/**
 * TBD.
 * 
 * @param <T> 
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class ASIOServiceProvider <T extends ASIOExchange> extends TCPSessionBasedServiceProvider<T> {
	@ServiceFactorizer
	static ASIOServiceProvider createService (final ASIOService service, final ClassFactory<? extends ASIOExchange> instanceFactory){
		try {
			return new ASIOServiceProvider(instanceFactory);
		} catch (final IOException ex) {
			throw new Error(ex);
		}
	}
	
	@ServiceInitializer
	static void initializeService (final ASIOService service, final ASIOServiceProvider serviceProvider) throws Throwable {
		serviceProvider.setBindPort(service.port());
		serviceProvider.setBufferSize(service.bufferSize());
		serviceProvider.setOptions(service.options());
		serviceProvider.setUseDynamicBuffers(service.useDynamicBuffers());
	}
	
	
	
	private int                       bufferSize = 0;
	private ASIOExchangeConfigurator  configurator = (session) -> {};
	private ASIOOption[]              options = new ASIOOption[0];
	private boolean                   useDynamicBuffers = false;
	
	private final ClassFactory<T>     instanceFactory;
	private final ServerSocketChannel listener;
	private ASIOSecurityProvider      securityProvider = null;
	private final Selector            selector;
	
	
	
	public ASIOServiceProvider (final Class<T> instanceClass) throws IOException {
		this(ClassFactory.getClassFactory(instanceClass));
	}
	
	public ASIOServiceProvider (final ClassFactory<T> instanceFactory) throws IOException {
		super();
		
		this.instanceFactory = instanceFactory;
		
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
	
	private IOByteBuffer createBuffer (){
		if (this.useDynamicBuffers) 
			return new IODynamicByteBuffer(this.bufferSize);
		else {
			return new IOByteBuffer(this.bufferSize);
		}
	}
	
	private T createInstance (){
		final T session;
		
		session = this.instanceFactory.newInstance();
		
		this.configurator.configure(session);
		
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
	
	private T getSessionForSelectionKey (final SelectionKey key){
		for (final var session : sessions) {
			if (session.channel() == key.channel()) {
				return session;
			}
		}
		
		return null;
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
		this.securityProvider = (ArrayUtilities.contains(this.options, ASIOOption.ENFORCE_TLS) ? new ASIOSecurityProvider() : null);
		this.listener.bind(new InetSocketAddress("0.0.0.0", this.getBindPort()), 0);
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
						channel.configureBlocking(false);
						channel.register(selector, (SelectionKey.OP_READ | SelectionKey.OP_WRITE));
						
						session = this.createInstance();
						session.setExecutor(this.getExecutor());
						session.initializeBuffers(this.createBuffer());
						session.initializeLogger(this.getLogger());
						session.onCreate();
						session.initialize(channel);
						session.onConnect();
						
						this.getLogger().log("session (%s) created".formatted(session));
						
						sessions.add(session);
					}
				} else {
					session = getSessionForSelectionKey(key);
					
					if (null == session) 
						continue;
					
					if (session.isAuthenticationCompleted() && session.isTimedOut()) {
						this.getLogger().log("session (%s) timed out".formatted(session));
						
						session.disconnect();
					}
					
					if (session.isDisconnected()) {
						handleDisconnect(session, null);
						
						continue;
					}
					
					try {
						if (key.isReadable()) {
							if (!session.isAuthenticationCompleted()) {
								if (session.isAuthenticationRequested()) {
									if (session.completeAuth(this.securityProvider, this.options)) {
										this.getLogger().log("session (%s) accepted OPTIONS and completed AUTH".formatted(session));
									} else {
										this.getLogger().log("session (%s) declined OPTIONS and canceled AUTH, it will be disconnected".formatted(session));
										
										handleDisconnect(session, null);
									}
								}
								
								continue;
							}
							
							session.listen();
						}
						
						if (key.isWritable()) {
							if (!session.isAuthenticationCompleted()) {
								if (! session.isAuthenticationRequested()) {
									session.requestAuth(this.securityProvider, this.options);
									
									this.getLogger().log("session (%s) requested AUTH with OPTIONS".formatted(session));
								}
								
								continue;
							}
							
							if (((ASIOOption.mergeOptions(this.options) & ASIOOption.KEEP_ALIVE.flag()) != 0) && session.isSuspect()) {
								if (! session.isPingWaiting()) {
									session.ping();
								}
								
								continue;
							}
							
							session.dispatch();
						}
					} catch (final Throwable t) {
						t.printStackTrace();
						
						handleDisconnect(session, t);
					}
				}
			}
		} catch (final IOException ex) {
			ex.printStackTrace();
		}
	}
	
	public void setBufferSize (final int bufferSize){
		this.bufferSize = bufferSize;
	}
	
	public void setConfigurator (final ASIOExchangeConfigurator configurator){
		this.configurator = configurator;
	}
	
	public void setOptions (final ASIOOption... options){
		this.options = options;
	}
	
	public void setUseDynamicBuffers (final boolean useDynamicBuffers){
		this.useDynamicBuffers = useDynamicBuffers;
	}
}