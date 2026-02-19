/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.net.sock.model.bash;

import com.xahico.boot.io.IOByteBuffer;
import com.xahico.boot.io.IODynamicByteBuffer;
import com.xahico.boot.net.sock.TCPSessionBasedServiceProvider;
import com.xahico.boot.net.sock.model.bare.BARESession;
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
import com.xahico.boot.pilot.ServiceFactorizer;
import com.xahico.boot.pilot.ServiceInitializer;
import com.xahico.boot.util.Exceptions;
import com.xahico.boot.util.OrderedEnumerator;
import java.util.function.Consumer;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class BASHServiceProvider extends TCPSessionBasedServiceProvider<BASHSession> {
	@ServiceFactorizer
	static BASHServiceProvider createService (final BASHService service, final ClassFactory<? extends BASHSession> instanceFactory){
		try {
			return new BASHServiceProvider(instanceFactory);
		} catch (final IOException ex) {
			throw new Error(ex);
		}
	}
	
	@ServiceInitializer
	static void initializeService (final BASHService service, final BASHServiceProvider serviceProvider) throws Throwable {
		serviceProvider.setBindPort(service.port());
		serviceProvider.setBufferSize(service.bufferSize());
		serviceProvider.setUseDynamicBuffers(service.useDynamicBuffers());
	}
	
	
	
	private int                                       bufferSize = 0;
	private BASHSessionConfigurator                   configurator = (session) -> {};
	private boolean                                   useDynamicBuffers = false;
	
	private final ClassFactory<? extends BASHSession> instanceFactory;
	private final ServerSocketChannel                 listener;
	private final Selector                            selector;
	private final List<BASHSession>                   sessions = new ArrayList<>();
	
	
	
	public BASHServiceProvider (final Class<? extends BASHSession> instanceClass) throws IOException {
		this(ClassFactory.getClassFactory(instanceClass));
	}
	
	public BASHServiceProvider (final ClassFactory<? extends BASHSession> instanceFactory) throws IOException {
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
	
	private BASHSession createInstance (){
		final BASHSession session;
		
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
	
	private BASHSession getSessionForSelectionKey (final SelectionKey key){
		for (final var session : sessions) {
			if (session.channel() == key.channel()) {
				return session;
			}
		}
		
		return null;
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
	public void manageSessions (final Consumer<BASHSession> handler, final Runnable callback){
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
	public void manageSessions (final OrderedEnumerator<BASHSession> handler, final Runnable callback){
		try {
			final Iterator<BASHSession> it;

			it = this.sessions.iterator();

			while (it.hasNext()) {
				final BASHSession session;

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
				final BASHSession   session;
				
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
						session.initializeBuffers(this.createBuffer());
						session.initializeLogger(this.getLogger());
						session.onCreate();
						session.initialize(this.getExecutor(), channel);
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
			ex.printStackTrace();
		}
	}
	
	public void setBufferSize (final int bufferSize){
		this.bufferSize = bufferSize;
	}
	
	public void setConfigurator (final BASHSessionConfigurator configurator){
		this.configurator = configurator;
	}
	
	public void setUseDynamicBuffers (final boolean useDynamicBuffers){
		this.useDynamicBuffers = useDynamicBuffers;
	}
}