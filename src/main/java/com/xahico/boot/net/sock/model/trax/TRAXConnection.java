/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.net.sock.model.trax;

import com.xahico.boot.dev.Helper;
import com.xahico.boot.lang.jsox.JSOXVariant;
import com.xahico.boot.net.inet.InetEndpoint;
import com.xahico.boot.net.sock.TCPSocket;
import com.xahico.boot.reflection.ClassFactory;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class TRAXConnection implements AutoCloseable {
	private static final int                          DEFAULT_RECONNECT_INTERVAL = 5000;
	
	private static final ClassFactory<TRAXConnection> CLASS_FACTORY = ClassFactory.getClassFactory(TRAXConnection.class);
	
	
	
	public static TRAXConnection newConnection (){
		return CLASS_FACTORY.newInstance();
	}
	
	
	
	private Runnable                   callbackOnConnect = () -> {};
	private Runnable                   callbackOnDisconnect = () -> {};
	
	private volatile boolean           abortCalled = false;
	private boolean                    autoReconnect = false;
	private int                        autoReconnectInterval = DEFAULT_RECONNECT_INTERVAL;
	private volatile boolean           closed = false;
	private volatile boolean           connected = false;
	private long                       connectScheduled;
	private TRAXConnectionErrorHandler errorHandler = null;
	private Executor                   executor = null;
	private InetSocketAddress          hostname = null;
	private final TCPSocket            socket = new TCPSocket();
	
	
	
	
	private TRAXConnection (){
		super();
	}
	
	
	
	public void abort (){
		this.abortCalled = true;
	}
	
	@Override
	public void close (){
		try {
			this.socket.close();
			
			if (this.connected && (null != this.callbackOnDisconnect)) {
				this.callbackOnDisconnect.run();
			}
		} finally {
			this.closed = true;
		}
	}
	
	private void connect (){
		final boolean tryConnect;
		
		tryConnect = ((System.currentTimeMillis() - connectScheduled) >= this.autoReconnectInterval);
		
		if (tryConnect) try {
			connectScheduled = System.currentTimeMillis();
			
			this.socket.connect(this.hostname);
			
			this.connected = true;
			
			if (null != this.callbackOnConnect) {
				this.callbackOnConnect.run();
			}
		} catch (final IOException ex) {
			this.handleError(ex);
		}
		
		if (!this.connected && this.autoReconnect && !this.isClosed() && !this.isAbortCalled()) {
			this.executor.execute(this::connect);
		}
	}
	
	protected Executor createDefaultExecutor (){
		return Executors.newSingleThreadExecutor();
	}
	
	private void ensureReady (){
		if (null == this.hostname) {
			throw new Error("host not set");
		}
		
		if (null == this.executor) {
			this.executor = this.createDefaultExecutor();
		}
	}
	
	public InetSocketAddress getHost (){
		return this.hostname;
	}
	
	private void handleError (final Throwable throwable){
		if (null != this.errorHandler) {
			this.errorHandler.call(throwable);
		}
	}
	
	public boolean isAbortCalled (){
		return this.abortCalled;
	}
	
	public boolean isClosed (){
		return this.closed;
	}
	
	public boolean isConnected (){
		return this.connected;
	}
	
	public void onConnect (final Runnable callback){
		this.callbackOnConnect = callback;
	}
	
	public void onDisconnect (final Runnable callback){
		this.callbackOnDisconnect = callback;
	}
	
	public void onError (final TRAXConnectionErrorHandler callback){
		this.errorHandler = callback;
	}
	
	public void open (){
		this.ensureReady();
		
		this.connectScheduled = (System.currentTimeMillis() - this.autoReconnectInterval);
		
		this.executor.execute(this::connect);
	}
	
	private JSOXVariant readObject () throws IOException {
		final StringBuilder buffer;
		final byte[]        packet;
		final int           packetSize;
		
		buffer = new StringBuilder();
		
		for (;;) {
			final char c;
			
			c = this.socket.receiveChar();
			
			if (c == TRAXSession.TERMINATOR) {
				packetSize = Integer.parseInt(buffer.toString());
				
				buffer.delete(0, buffer.length());
				
				break;
			} else {
				buffer.append(c);
			}
		}
		
		packet = new byte[packetSize];
		
		this.socket.receive(packet);
		
		return new JSOXVariant(new String(packet, TRAXSession.PROTO_CHARSET));
	}
	
	public void setAutoReconnect (final boolean enabled){
		this.autoReconnect = enabled;
	}
	
	public void setAutoReconnectInterval (final int timeMillis){
		this.autoReconnectInterval = timeMillis;
	}
	
	@Helper
	public void setHost (final InetEndpoint hostname){
		this.setHost(hostname.getAddressString(), hostname.getPort());
	}
	
	public void setHost (final InetSocketAddress hostname){
		this.hostname = hostname;
	}
	
	@Helper
	public void setHost (final String hostname){
		this.setHost(InetEndpoint.getByName(hostname));
	}
	
	@Helper
	public void setHost (final String address, final int port){
		this.setHost(new InetSocketAddress(address, port));
	}
	
	public void transact (final String control, final TransactionHandler handler){
		this.executor.execute(() -> {
			if (! this.connected) 
				this.transact(control, handler);
			else try {
				final TRAXRequest  request;
				final TRAXResponse response;

				request = handler.createRequest();

				handler.onRequest(request);

				this.writeObject(request.toVariant());

				response = handler.createResponse();
				response.assume(this.readObject());

				handler.onResponse(response);
			} catch (final IOException ex) {
				handler.onError(ex);
				
				this.handleError(ex);

				if (this.connected) {
					if (null != this.callbackOnDisconnect) {
						this.callbackOnDisconnect.run();
					}

					if (this.autoReconnect && !this.isClosed() && !this.isAbortCalled()) {
						this.executor.execute(this::connect);
					}
				}
			}
		});
	}
	
	private void writeObject (final JSOXVariant request) throws IOException {
		final byte[] requestBody;
		final byte[] requestHead;
		
		requestBody = request.toJSONStringCompact().getBytes(TRAXSession.PROTO_CHARSET);
		requestHead = (Integer.toString(requestBody.length) + TRAXSession.TERMINATOR).getBytes(TRAXSession.PROTO_CHARSET);
		
		this.socket.send(requestHead);
		this.socket.send(requestBody);
	}
	
	
	
	public static interface TransactionHandler <REQ extends TRAXRequest, RSP extends TRAXResponse> {
		REQ createRequest ();
		
		RSP createResponse ();
		
		void onError (final Throwable throwable);
		
		void onRequest (final REQ request);
		
		void onResponse (final RSP response);
	}
}