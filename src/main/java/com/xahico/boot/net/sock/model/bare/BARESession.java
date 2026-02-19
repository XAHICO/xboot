/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.net.sock.model.bare;

import com.xahico.boot.io.IOByteBuffer;
import com.xahico.boot.io.IODynamicByteBuffer;
import com.xahico.boot.io.IOSocketChannel;
import com.xahico.boot.lang.jsox.JSOXException;
import com.xahico.boot.lang.jsox.JSOXVariant;
import com.xahico.boot.pilot.Session;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public abstract class BARESession extends Session {
	public static final long INTERVAL_HEARTBEAT = 10000; // 60000 // 1 minute
	
	
	
	private final IOByteBuffer          bufferIn = new IODynamicByteBuffer().charset(UTF_8);
	private final IOByteBuffer          bufferOut = new IODynamicByteBuffer().charset(UTF_8);
	private IOSocketChannel             channel = null;
	private boolean                     disconnected = false;
	private long                        lastContact = -1;
	private Set<BARETransactionModel>   transactionModels = null;
	private final List<BARETransaction> transactions = new ArrayList<>();
	
	
	
	protected BARESession (){
		super();
	}
	
	
	
	final SocketChannel channel (){
		return this.channel.wrappedChannel();
	}
	
	final void clearTransactions (){
		this.transactions.clear();
	}
	
	@BAREControl("heartbeat")
	private void completeHeartbeat (final BAREWireTestRequest request, final BAREWireTestResponse response){
		this.markContact();
	}
	
	final void destroy (){
		try {
			this.channel.close();
		} finally {
			this.onDestroy();
		}
	}
	
	public final void disconnect (){
		this.call(() -> this.markDisconnected());
	}
	
	private void dispatch (final BARETransaction transaction) throws IOException {
		try {
			transaction.onRequest();
			
			transaction.prepare();
			
			bufferOut.head();
			bufferOut.putObject(transaction.getRequest());
			bufferOut.write(channel);
			bufferOut.compact();
			
			this.markContact();
		} catch (final JSOXException ex) {
			this.onError(ex);
		}
	}
	
	final void dispatchAll () throws IOException {
		for (final var transaction : transactions) {
			if (transaction.isUnhandled()) {
				dispatch(transaction);
			}
		}
	}
	
	public final String getAddress (){
		try {
			return this.channel.wrappedChannel().getRemoteAddress().toString();
		} catch (final IOException ex) {
			return null;
		}
	}
	
	private BARETransactionModel getTransactionModel (final String control){
		for (final var transactionModel : this.transactionModels) {
			if (transactionModel.control().equalsIgnoreCase(control)) {
				return transactionModel;
			}
		}
		
		return null;
	}
	
	final void initializeChannel (final SocketChannel channel){
		this.channel = IOSocketChannel.wrap(channel);
	}
	
	final void initializeTransactionModels (final Set<BARETransactionModel> transactionModels){
		this.transactionModels = transactionModels;
	}
	
	public final boolean isDisconnected (){
		return this.disconnected;
	}
	
	public boolean isSuspect (){
		return ((System.currentTimeMillis() - this.lastContact) >= INTERVAL_HEARTBEAT);
	}
	
	final void listen () throws IOException {
		final long                      bytesRead;
		final Iterator<BARETransaction> it;
		final JSOXVariant               packet;
		final BARETransactResponse      packetConstructed;
		
		if (this.transactions.isEmpty()) {
			throw new IOException();
		}
		
		bufferIn.head();
		
		bytesRead = this.channel.read(bufferIn);
		
		if (bytesRead > 0) try {
			bufferIn.rewind();
			
			packet = bufferIn.getObject();
			
			bufferIn.compact();
			
			this.markContact();
			
			packetConstructed = packet.castTo(BARETransactResponse.class);
			
			it = this.transactions.iterator();
			
			while (it.hasNext()) {
				final BARETransaction transaction;
				
				transaction = it.next();
				
				if (packetConstructed.transactionId.equals(transaction.getId())) {
					it.remove();
					
					transaction.getResponse().transactionId = transaction.getId();
					transaction.getResponse().response.assume(packetConstructed.response);
					
					transaction.call();
					
					transaction.onResponse();
					
					transaction.complete();
					
					System.out.println("[%s] Completed Transaction '%s'".formatted(this, transaction.getId()));
					System.out.println("Request:");
					System.out.println(transaction.getRequest());
					System.out.println("Response:");
					System.out.println(transaction.getResponse());
				}
			}
		} catch (final JSOXException ex) {
			this.onError(ex);
		}
	}
	
	private void markContact (){
		this.lastContact = System.currentTimeMillis();
	}
	
	final void markDisconnected (){
		this.disconnected = true;
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
		
	}
	
	protected void onError (final Throwable throwable){
		
	}
	
	public final void testHeartbeat (){
		this.transact("heartbeat", new BARECallback<BAREWireTestRequest, BAREWireTestResponse>() {
			@Override
			public void onRequest (final BAREWireTestRequest request){
				
			}
			
			@Override
			public void onResponse (final BAREWireTestResponse response){
				
			}
		});
	}
	
	public final BARETransaction transact (final String control, final BARECallback callback){
		final BARETransaction      transaction;
		final BARETransactionModel transactionModel;
		
		transactionModel = getTransactionModel(control);
		
		if (null == transactionModel) {
			throw new Error("no transaction model for '%s:%s'".formatted(this.getClass().getName(), control));
		}
		
		transaction = transactionModel.newTransaction(this);
		transaction.setCallback(callback);
		
		this.call(() -> transactions.add(transaction));
		
		return transaction;
	}
}