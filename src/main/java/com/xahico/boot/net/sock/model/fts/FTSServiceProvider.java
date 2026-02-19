/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.net.sock.model.fts;

import com.xahico.boot.net.sock.TCPSessionBasedServiceProvider;
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
import com.xahico.boot.platform.FileUtilities;
import com.xahico.boot.util.Exceptions;
import com.xahico.boot.util.OrderedEnumerator;
import java.io.File;
import java.util.Collections;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class FTSServiceProvider extends TCPSessionBasedServiceProvider<FTSSession> {
	@ServiceFactorizer
	static FTSServiceProvider createService (final FTSService service, final ClassFactory<? extends FTSSession> instanceFactory){
		try {
			return new FTSServiceProvider(instanceFactory);
		} catch (final IOException ex) {
			throw new Error(ex);
		}
	}
	
	@ServiceInitializer
	static void initializeService (final FTSService service, final FTSServiceProvider serviceProvider) throws Throwable {
		serviceProvider.setBindPort(service.port());
		serviceProvider.setBufferSize(service.bufferSize());
	}
	
	
	
	private int                                      bufferSize = 0;
	private FTSSessionConfigurator                   configurator = (session) -> {};
	private File                                     directory = null;
	
	private final ClassFactory<? extends FTSSession> instanceFactory;
	private final ServerSocketChannel                listener;
	private final FTSSecurityProvider                securityProvider = new FTSSecurityProvider();
	private final Selector                           selector;
	private final List<FTSUpload>                    uploads = Collections.synchronizedList(new ArrayList<>());
	
	
	
	public FTSServiceProvider (final Class<? extends FTSSession> instanceClass) throws IOException {
		this(ClassFactory.getClassFactory(instanceClass));
	}
	
	public FTSServiceProvider (final ClassFactory<? extends FTSSession> instanceFactory) throws IOException {
		super();
		
		this.instanceFactory = instanceFactory;
		
		this.selector = Selector.open();
		
		this.listener = ServerSocketChannel.open();
	}
	
	
	
	private void checkUploads (){
		final Iterator<FTSUpload> it;
		
		synchronized (this.uploads) {
			it = this.uploads.iterator();

			while (it.hasNext()) {
				final FTSUpload upload;

				upload = it.next();

				if (upload.isCompleted()) try {
					upload.complete();
					
					continue;
				} finally {
					it.remove();
				}

				if (upload.canDiscard()) try {
					upload.discard();
				} finally {
					it.remove();
				}
			}
		}
	}
	
	@Override
	protected void cleanup (){
		if (null != this.listener) try {
			this.listener.close();
		} catch (final IOException ex) {
			Exceptions.ignore(ex);
		}
	}
	
	private FTSSession createInstance (){
		final FTSSession session;
		
		session = this.instanceFactory.newInstance();
		
		this.configurator.configure(session);
		
		return session;
	}
	
	public final String createUpload (final FTSUploadCallbacks callbacks){
		final String key;
		
		key = UUID.randomUUID().toString();
		
		return this.createUpload(key, callbacks);
	}
	
	public final String createUpload (final String key, final FTSUploadCallbacks callbacks){
		final FTSUpload upload;
		
		upload = new FTSUpload(this.directory, key, callbacks);
		
		this.uploads.add(upload);
		
		return upload.key();
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
	
	private FTSSession getSessionForSelectionKey (final SelectionKey key){
		for (final var session : sessions) {
			if (session.channel() == key.channel()) {
				return session;
			}
		}
		
		return null;
	}
	
	@Override
	protected void initialize () throws Throwable {
		if (! this.directory.exists()) try {
			FileUtilities.createDirectories(this.directory);
		} catch (final IOException ex) {
			throw new Error(ex);
		} else try {
			FileUtilities.deleteDirectoryContents(this.directory);
		} catch (final IOException ex) {
			Exceptions.ignore(ex);
		}
		
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
				final FTSSession    session;
				
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
						session.initializeUploadsShared(this.uploads);
						session.initializeSecurityProvider(this.securityProvider);
						session.initializeBuffers(this.bufferSize);
						session.initializeLogger(this.getLogger());
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
							session.talk();
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
			
			this.checkUploads();
		} catch (final IOException ex) {
			ex.printStackTrace();
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
	
	public void setBufferSize (final int bufferSize){
		this.bufferSize = bufferSize;
	}
	
	public void setConfigurator (final FTSSessionConfigurator configurator){
		this.configurator = configurator;
	}
	
	public void setDirectory (final File directory){
		this.directory = directory;
	}
}