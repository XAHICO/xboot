/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.net.sock.model.trax;

import com.xahico.boot.synchronicity.ClockedIntensity;
import com.xahico.boot.net.sock.TCPServiceProvider;
import com.xahico.boot.util.Exceptions;
import com.xahico.boot.reflection.ClassFactory;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import com.xahico.boot.pilot.ServiceFactorizer;
import com.xahico.boot.pilot.ServiceInitializer;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class TRAXServiceProvider extends TCPServiceProvider {
	@ServiceFactorizer
	static TRAXServiceProvider createService (final TRAXService service, final ClassFactory<? extends TRAXSession> classFactory){
		try {
			return new TRAXServiceProvider(classFactory, TRAXUtilities.getControlMethods(classFactory.getProductionClass()));
		} catch (final IOException ex) {
			throw new Error(ex);
		}
	}
	
	@ServiceInitializer
	static void initializeService (final TRAXService service, final TRAXServiceProvider serviceProvider) throws Throwable {
		serviceProvider.setBindPort(service.port());
		serviceProvider.setBufferSize(service.bufferSize());
		
		if (service.multithreaded()) {
			serviceProvider.setExecutor(Executors.newCachedThreadPool());
		} else {
			serviceProvider.setExecutor(Executors.newSingleThreadExecutor());
		}
	}
	
	
	
	private int                                       bufferSize = 0;
	private final ClassFactory<? extends TRAXSession> instanceFactory;
	private ClockedIntensity                          intensity = ClockedIntensity.MEDIUM;
	private final ServerSocketChannel                 listener;
	private final Set<TRAXMethod>                     methods;
	private final Selector                            selector;
	private final List<TRAXSession>                   sessions = new ArrayList<>();
	
	
	
	private TRAXServiceProvider (final ClassFactory<? extends TRAXSession> instanceFactory, final Set<TRAXMethod> methods) throws IOException {
		super();
		
		this.instanceFactory = instanceFactory;
		
		this.methods = methods;
		
		this.selector = Selector.open();
		
		this.listener = ServerSocketChannel.open();
	}
	
	
	
	@Override
	protected void cleanup (){
		try {
			sessions.forEach(session -> session.destroy());
		} finally {
			sessions.clear();
		}

		try {
			this.selector.close();
		} catch (final IOException ex) {
			Exceptions.ignore(ex);
		}

		try {
			this.listener.close();
		} catch (final IOException ex) {
			Exceptions.ignore(ex);
		}
	}
	
	@Override
	protected Executor createDefaultExecutor (){
		return Executors.newSingleThreadExecutor();
	}
	
	private TRAXSession createInstance (){
		return this.instanceFactory.newInstance();
	}
	
	@Override
	public int getPort (){
		try {
			final InetSocketAddress inetAddress;
			
			inetAddress = (InetSocketAddress) this.listener.getLocalAddress();
			
			if (null == inetAddress) 
				return -1;
			
			return inetAddress.getPort();
		} catch (final IOException ex) {
			return -1;
		}
	}
	
	private TRAXSession getSessionForSelectionKey (final SelectionKey key){
		for (final var session : sessions) {
			if (session.channel() == key.channel()) {
				return session;
			}
		}
		
		return null;
	}
	
	@Override
	protected void initialize () throws Throwable {
		this.listener.socket().bind(new InetSocketAddress("0.0.0.0", this.getBindPort()), 0);
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
				final TRAXSession   session;
				
				if (this.isStopped()) {
					break;
				}

				key = it.next();

				it.remove();
				
				if (key.channel() instanceof ServerSocketChannel) {
					if (key.isAcceptable()) {
						channel = listener.accept();
						
						channel.configureBlocking(false);
						channel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
						
						session = this.createInstance();
						session.initializeBuffers(this.bufferSize);
						session.initializeLogger(this.getLogger());
						session.onCreate();
						session.initialize(this.getExecutor(), channel, this.methods);
						session.onConnect();
						
						this.getLogger().log("session (%s) created".formatted(session));

						sessions.add(session);
					}
				} else {
					session = getSessionForSelectionKey(key);

					if (null == session) 
						continue;
					
					if (session.isDisconnected()) {
						session.onDisconnect();
						session.destroy();
						session.onDestroy();
						
						sessions.remove(session);
						
						continue;
					}
					
					try {
						if (key.isReadable()) {
							session.listen();
						}

						if (key.isWritable()) {
							session.dispatchAll();
						}
					} catch (final IOException ex) {
						this.getLogger().log(ex, "session (%s) has disconnected".formatted(session));
						
						session.markDisconnected();
						session.onDisconnect();
						session.destroy();
						session.onDestroy();

						sessions.remove(session);
					}
				}
			}
		} catch (final IOException ex) {
			this.getLogger().log(ex);
		}
	}
	
	public void setBufferSize (final int bufferSize){
		this.bufferSize = bufferSize;
	}
	
	public void setIntensity (final ClockedIntensity intensity){
		this.intensity = intensity;
	}
	
	private void sleep () throws InterruptedException {
		final long waitTime;
		
		if (this.isIdle())
			waitTime = this.intensity.idleClock();
		else {
			waitTime = this.intensity.activeClock(this.sessions.size());
		}
		
		Thread.sleep(waitTime);
	}
}