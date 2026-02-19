/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.net.sock.model.cortex;

import com.xahico.boot.io.IOByteBuffer;
import com.xahico.boot.io.IODynamicByteBuffer;
import com.xahico.boot.io.IOSocketChannel;
import com.xahico.boot.lang.jsox.JSOXObject;
import com.xahico.boot.pilot.Session;
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
public class CORTEXSession <T extends CORTEXEvent> extends Session {
	private static final Charset USE_CHARSET = StandardCharsets.UTF_8;
	
	
	
	private IOByteBuffer                    bufferIn = null;
	private IOByteBuffer                    bufferOut = null;
	private IOSocketChannel                 channel = null;
	private boolean                         connected = false;
	private boolean                         disconnected = false;
	private Map<String, Class<? extends T>> eventMap = null;
	private Set<CORTEXMethod>               methods = null;
	private final Set<CORTEXTransaction>    transactions = Collections.synchronizedSet(new HashSet<>());
	
	
	
	protected CORTEXSession (){
		super();
	}
	
	
	
	final SocketChannel channel (){
		return this.channel.wrappedChannel();
	}
	
	final void destroy (){
		this.channel.close();
	}
	
	final void dispatch () throws IOException {
		this.bufferOut.head();
		
		synchronized (transactions) {
			for (final var transaction : transactions) {
				final CORTEXPacket packet;
				final byte[]      packetBytes;
				final int         packetSize;
				
				if (transaction.isRequestSent()) 
					continue;
				
				transaction.invokeRequest();
				
				packet = new CORTEXPacket();
				packet.control = transaction.getControl();
				packet.data = transaction.getRequest().toVariant();
				packet.id = transaction.getId();
				packet.type = CORTEXPacket.Type.TRANSACTION;
				
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
	
	final void initializeMethods (final Set<CORTEXMethod> methods){
		this.methods = methods;
	}
	
	public boolean isConnected (){
		return this.connected;
	}
	
	public boolean isDisconnected (){
		return this.disconnected;
	}
	
	final void listen () throws IOException {
		this.bufferIn.head();
		
		this.channel.read(this.bufferIn);
		
		this.bufferIn.rewind();
		
		while (this.bufferIn.remaining() > (Integer.BYTES + 2)) {
			final CORTEXPacket packet;
			final byte[]       packetBytes;
			final int          packetSize;
			
			packetSize = this.bufferIn.getInteger();
			
			if (this.bufferIn.remaining() < packetSize) {
				break;
			}
			
			packetBytes = this.bufferIn.getBytes(packetSize);

			packet = JSOXObject.newInstanceOf(CORTEXPacket.class, packetBytes, USE_CHARSET);
			
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
					final CORTEXTransaction transaction;
					
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
	
	final void markConnected (final boolean state){
		this.connected = state;
	}
	
	final void markDisconnected (final boolean state){
		this.disconnected = state;
	}
	
	protected void onBroadcast (final String eventCode, final T event){
		
	}
	
	protected void onConnect (){
		
	}
	
	@Override
	protected void onCreate (){
		
	}
	
	@Override
	protected void onDestroy (){
		
	}
	
	protected void onDisconnect (){
		this.bufferIn.clear();
		this.bufferOut.clear();
		this.transactions.clear();
	}
	
	protected void onUnhandledPacket (final CORTEXPacket packet){
		
	}
	
	public void transact (final String control, final CORTEXTransactionConfigurator configurator){
		final CORTEXMethod<?, ?> method;
		final CORTEXRequest      request;
		final CORTEXResponse     response;
		
		method = CollectionUtilities.seek(methods, (__) -> (__.getControl().equals(control)), false);
		
		assert(null != method);
		
		request = JSOXObject.newInstanceOf(method.getRequestClass());
		
		response = JSOXObject.newInstanceOf(method.getResponseClass());
		
		synchronized (transactions) {
			final CORTEXTransaction transaction;
			final int              transactionId;
			
			transactionId = transactions.size();
			
			transaction = new CORTEXTransaction(transactionId, method, request, response);
			
			configurator.configure(transaction);
			
			assert(transaction.validate());
			
			transactions.add(transaction);
		}
	}
}