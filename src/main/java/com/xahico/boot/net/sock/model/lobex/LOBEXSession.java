/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.net.sock.model.lobex;

import com.xahico.boot.io.IOByteBuffer;
import com.xahico.boot.io.IODynamicByteBuffer;
import com.xahico.boot.io.IOSocketChannel;
import com.xahico.boot.lang.jsox.JSOXObject;
import com.xahico.boot.net.NetSession;
import com.xahico.boot.util.CollectionUtilities;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
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
public class LOBEXSession <T extends LOBEXEvent> extends NetSession {
	private static final Charset USE_CHARSET = StandardCharsets.UTF_8;
	
	
	
	private IOByteBuffer                    bufferIn = null;
	private IOByteBuffer                    bufferOut = null;
	private IOSocketChannel                 channel = null;
	private boolean                         disconnected = false;
	private Map<String, Class<? extends T>> eventMap = null;
	private Set<LOBEXMethod>                methods = null;
	private final Set<LOBEXTransaction>     transactions = Collections.synchronizedSet(new HashSet<>());
	
	
	
	protected LOBEXSession (){
		super();
	}
	
	
	
	final SocketChannel channel (){
		return this.channel.wrappedChannel();
	}
	
	final void destroy (){
		try {
			this.channel.close();
		} finally {
			synchronized (transactions) {
				for (final var transaction : transactions) {
					if (! transaction.isCompleted()) {
						transaction.invokeDiscard();
					}
				}

				transactions.clear();
			}
		}
	}
	
	final void dispatch () throws IOException {
		this.bufferOut.head();
		
		synchronized (transactions) {
			for (final var transaction : transactions) {
				final LOBEXPacket packet;
				final byte[]      packetBytes;
				final int         packetSize;
				
				if (transaction.isRequestSent()) 
					continue;
				
				transaction.invokeRequest();
				
				packet = new LOBEXPacket();
				packet.control = transaction.getControl();
				packet.data = transaction.getRequest().toVariant();
				packet.id = transaction.getId();
				packet.type = LOBEXPacket.Type.TRANSACTION;
				
				packetBytes = packet.toJSONStringCompact().getBytes(USE_CHARSET);
				
				packetSize = packetBytes.length;
				
				this.bufferOut.putInteger(packetSize);
				this.bufferOut.putBytes(packetBytes);
				
				transaction.markRequestSent();
			}
		}
		
		this.bufferOut.rewind();
		
		this.channel.write(this.bufferOut);
		
		this.bufferOut.compact();
	}
	
	@Override
	public void disconnect (){
		this.markDisconnected();
	}
	
	final void initializeBuffers (final int bufferSize){
		this.bufferIn = new IODynamicByteBuffer(bufferSize);
		this.bufferOut = new IODynamicByteBuffer(bufferSize);
	}
	
	final void initializeChannel (final SocketChannel channel){
		this.channel = IOSocketChannel.wrap(channel);
	}
	
	final void initializeEvents (final Map<String, Class<? extends T>> eventMap){
		this.eventMap = eventMap;
	}
	
	final void initializeMethods (final Set<LOBEXMethod> methods){
		this.methods = methods;
	}
	
	@Override
	public boolean isDisconnected (){
		return this.disconnected;
	}
	
	final void listen () throws IOException {
		this.bufferIn.head();
		
		this.channel.read(this.bufferIn);
		
		this.bufferIn.rewind();
		
		while (this.bufferIn.remaining() > (Integer.BYTES + 2)) {
			final LOBEXPacket packet;
			final byte[]      packetBytes;
			final int         packetSize;
			
			packetSize = this.bufferIn.getInteger();
			
			if (this.bufferIn.remaining() < packetSize) {
				break;
			}
			
			packetBytes = this.bufferIn.getBytes(packetSize);

			packet = JSOXObject.newInstanceOf(LOBEXPacket.class, packetBytes, USE_CHARSET);

			switch (packet.type) {
				case EVENT: {
					final T                  event;
					final Class<? extends T> eventClass;
					final String             eventCode;
					
					eventCode = packet.control;
					
					eventClass = eventMap.get(eventCode);
					
					if (null != eventClass) {
						event = JSOXObject.newInstanceOf(eventClass, packet.data);
						
						this.onBroadcast(eventCode, event);
					} else {
						this.onUnhandledPacket(packet);
					}
					
					break;
				}
				case TRANSACTION: {
					final LOBEXTransaction transaction;
					
					synchronized (transactions) {
						transaction = CollectionUtilities.seek(transactions, (__) -> (__.getId() == packet.id), true);
					}
					
					if (null == transaction) {
						this.onUnhandledPacket(packet);
					} else {
						transaction.getResponse().assume(packet.data);
						
						transaction.markResponseReceived();
						
						if (packet.status) {
							transaction.invokeResponse();
						} else {
							transaction.invokeDiscard();
						}
					}

					break;
				}
			}

			this.bufferIn.compact();
		}
	}
	
	final void markDisconnected (){
		this.disconnected = true;
	}
	
	protected void onBroadcast (final String eventCode, final T event){
		
	}
	
	@Override
	protected void onConnect (){
		
	}
	
	@Override
	protected void onCreate (){
		
	}
	
	@Override
	protected void onDestroy (){
		
	}
	
	@Override
	protected void onDisconnect (){
		
	}
	
	protected void onUnhandledPacket (final LOBEXPacket packet){
		
	}
	
	public void transact (final String control, final LOBEXTransactionConfigurator configurator){
		final LOBEXMethod<?, ?> method;
		final LOBEXRequest      request;
		final LOBEXResponse     response;
		
		method = CollectionUtilities.seek(methods, (__) -> (__.getControl().equals(control)), false);
		
		assert(null != method);
		
		request = JSOXObject.newInstanceOf(method.getRequestClass());
		
		response = JSOXObject.newInstanceOf(method.getResponseClass());
		
		synchronized (transactions) {
			final LOBEXTransaction transaction;
			final int              transactionId;
			
			transactionId = transactions.size();
			
			transaction = new LOBEXTransaction(transactionId, method, request, response);
			
			configurator.configure(transaction);
			
			assert(transaction.validate());
			
			transactions.add(transaction);
		}
	}
	
	
	
	@FunctionalInterface
	public static interface RequestBuilder <T extends LOBEXRequest> {
		void prep(T obj);
	}
}