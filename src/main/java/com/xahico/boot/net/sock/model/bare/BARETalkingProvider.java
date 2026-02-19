/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.net.sock.model.bare;

import com.xahico.boot.net.sock.TCPSessionBasedServiceProvider;
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
import com.xahico.boot.util.OrderedEnumerator;
import java.util.function.Consumer;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class BARETalkingProvider extends TCPSessionBasedServiceProvider<BARESession> {
	@ServiceFactorizer
	static BARETalkingProvider createService (final BARETalker service, final ClassFactory<? extends BARESession> classFactory){
		try {
			return new BARETalkingProvider(classFactory, BAREUtilities.getControlMethods(classFactory.getProductionClass()));
		} catch (final IOException ex) {
			throw new Error(ex);
		}
	}
	
	@ServiceInitializer
	static void initializeService (final BARETalker service, final BARETalkingProvider serviceProvider) throws Throwable {
		serviceProvider.setBindPort(service.port());
	}
	
	
	
	private final ClassFactory<? extends BARESession> instanceFactory;
	private final ServerSocketChannel                 listener;
	private final Selector                            selector;
	private final List<BARESession>                   sessions = new ArrayList<>();
	private final Set<BARETransactionModel>           transactionModels;
	
	
	
	private BARETalkingProvider (final ClassFactory<? extends BARESession> instanceFactory, final Set<BAREMethod> methods) throws IOException {
		super();
		
		this.instanceFactory = instanceFactory;
		
		this.transactionModels = BAREUtilities.loadTransactionModels(methods);
		
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
	
	private BARESession createInstance (){
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
	
	private BARESession getSessionForSelectionKey (final SelectionKey key){
		for (final var session : sessions) {
			if (session.channel() == key.channel()) {
				return session;
			}
		}
		
		return null;
	}
	
	private void handleDisconnect (final BARESession session){
		session.clearTransactions();
		
		if (! session.isDisconnected()) {
			session.markDisconnected();
		}
		
		try {
			session.onDisconnect();
			session.destroy();
		} finally {
			sessions.remove(session);
		}
	}
	
	@Override
	protected void initialize () throws Throwable {
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
	public void manageSessions (final Consumer<BARESession> handler, final Runnable callback){
		try {
			for (final var session : this.sessions) {
				handler.accept(session);
			}
		} finally {
			if (null != callback) {
				callback.run();
			}
		}
	}
	
	@Override
	public void manageSessions (final OrderedEnumerator<BARESession> handler, final Runnable callback){
		try {
			final Iterator<BARESession> it;

			it = this.sessions.iterator();

			while (it.hasNext()) {
				final BARESession session;

				session = it.next();

				if (! handler.accept(session)) {
					break;
				}
			}
		} finally {
			if (null != callback) {
				callback.run();
			}
		}
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
				final BARESession   session;
				
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
						session.setExecutor(this.getExecutor());
						session.initializeTransactionModels(transactionModels);
						session.onCreate();
						session.initializeChannel(channel);
						session.onConnect();
						
						this.getLogger().log("session (%s) created".formatted(session));

						sessions.add(session);
					}
				} else {
					session = getSessionForSelectionKey(key);

					if (null == session) 
						continue;
					
					if (session.isDisconnected()) {
						handleDisconnect(session);
						
						continue;
					}
					
					try {
						if (key.isReadable()) {
							session.listen();
						}

						if (key.isWritable()) {
							session.dispatchAll();
						}
						
						if (session.isSuspect()) {
							session.testHeartbeat();
						}
					} catch (final IOException ex) {
						this.getLogger().log(ex, "session (%s) has disconnected".formatted(session));
						
						handleDisconnect(session);
					}
				}
			}
		} catch (final IOException ex) {
			this.getLogger().log(ex);
		}
	}
}