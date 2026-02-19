/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.net.sock.model.bare;

import com.xahico.boot.net.inet.InetEndpoint;
import com.xahico.boot.net.sock.TCPInstancedServiceProvider;
import com.xahico.boot.util.Exceptions;
import com.xahico.boot.reflection.ClassFactory;
import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
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
public final class BAREListeningProvider extends TCPInstancedServiceProvider<BAREConnection> {
	@ServiceFactorizer
	static BAREListeningProvider createService (final BAREListener service, final ClassFactory<? extends BAREConnection> classFactory){
		return new BAREListeningProvider(classFactory, BAREUtilities.getControlMethods(classFactory.getProductionClass()));
	}
	
	@ServiceInitializer
	static void initializeService (final BAREListener service, final BAREListeningProvider serviceProvider) throws Throwable {
		serviceProvider.setAutoReconnect(service.autoReconnect());
		serviceProvider.setAutoReconnectInterval(service.autoReconnectInterval());
		serviceProvider.setHostname(service.host());
	}
	
	
	
	private boolean                                      autoReconnect = false;
	private int                                          autoReconnectInterval = -1;
	private String                                       hostname = null;
	
	private BAREConnection                               connection = null;
	private long                                         connectScheduled = -1;
	private final ClassFactory<? extends BAREConnection> instanceFactory;
	private SocketChannel                                listener = null;
	private Selector                                     selector = null;
	private final Set<BARETransactionModel>              transactionModels;
	
	
	
	private BAREListeningProvider (final ClassFactory<? extends BAREConnection> instanceFactory, final Set<BAREMethod> methods){
		super();
		
		this.instanceFactory = instanceFactory;
		
		this.transactionModels = BAREUtilities.loadTransactionModels(methods);
	}
	
	
	
	@Override
	protected void cleanup (){
		if (null != this.connection) {
			this.connection.close();
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
	
	private boolean connect (){
		boolean canConnect = false;
		boolean connected = false;
		
		if (connectScheduled == -1) {
			connectScheduled = System.currentTimeMillis();
			
			canConnect = true;
		}
		
		if ((System.currentTimeMillis() - this.connectScheduled) >= this.autoReconnectInterval) {
			connectScheduled = -1;
		}
		
		if (canConnect) try {
			this.selector = Selector.open();
			
			this.listener = SocketChannel.open();
			
			this.listener.configureBlocking(false);
			
			this.listener.register(selector, SelectionKey.OP_CONNECT | SelectionKey.OP_READ | SelectionKey.OP_WRITE);
			
			this.listener.connect(InetEndpoint.getByName(this.hostname).toSocketAddress());
			
			this.connection.initializeChannel(this.listener);
			this.connection.initializeTransactionModels(this.transactionModels);
			this.connection.markConnected(true);
			
			connected = true;
		} catch (final IOException ex) {
			this.getLogger().log(ex);
		}
		
		if (!connected && this.autoReconnect && !this.connection.isClosed()) {
			this.call(this::connect);
		}

		return connected;
	}
	
	private BAREConnection createInstance (){
		return this.instanceFactory.newInstance();
	}
	
	@Override
	protected Executor createDefaultExecutor (){
		return Executors.newSingleThreadExecutor();
	}
	
	@Override
	public BAREConnection getInstance (){
		return this.connection;
	}
	
	@Override
	public int getPort (){
		return 0;
	}
	
	private void handleDisconnect (){
		this.connection.clearTransactions();
		
		this.connection.markConnected(false);
		
		if (this.autoReconnect && !this.connection.isClosed()) {
			this.call(this::connect);
		}
	}
	
	@Override
	protected void initialize () throws Throwable {
		this.connection = this.createInstance();
		this.connection.setExecutor(this.getExecutor());
		
		this.connect();
	}
	
	@Override
	public boolean isIdle (){
		return !this.connection.isConnected();
	}
	
	@Override
	public boolean isStepper (){
		return true;
	}
	
	@Override
	protected void run (){
		if (this.connection.isConnected()) try {
			final Iterator<SelectionKey> it;
			
			selector.selectNow();
			
			it = selector.selectedKeys().iterator();
			
			while (it.hasNext()) {
				final SelectionKey key;
				
				if (this.isStopped()) {
					break;
				}

				key = it.next();
				
				it.remove();
				
				if (key.isConnectable()) {
					this.connection.finishConnect();
					
					continue;
				}
				
				if (key.isReadable()) {
					this.connection.listen();
				}
				
				if (key.isWritable()) {
					this.connection.dispatchAll();
				}
			}
			
			if (this.connection.isSuspect()) {
				this.connection.reset();
				
				handleDisconnect();
			}
		} catch (final IOException ex) {
			this.getLogger().log(ex);
			
			handleDisconnect();
		}
	}
	
	public void setAutoReconnect (final boolean enabled){
		this.autoReconnect = enabled;
	}
	
	public void setAutoReconnectInterval (final int timeMillis){
		this.autoReconnectInterval = timeMillis;
	}
	
	public void setHostname (final String hostname){
		this.hostname = hostname;
	}
}