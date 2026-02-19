/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.net.sock.model.bare;

import com.xahico.boot.io.IOByteBuffer;
import com.xahico.boot.io.IODynamicByteBuffer;
import com.xahico.boot.io.IOSocketChannel;
import com.xahico.boot.lang.jsox.IncompleteObjectException;
import com.xahico.boot.lang.jsox.JSOXException;
import com.xahico.boot.pilot.Session;
import com.xahico.boot.util.Exceptions;
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
public abstract class BAREConnection extends Session {
	public static final long TIMEOUT_HEARTBEAT = (BARESession.INTERVAL_HEARTBEAT + 10000);
	
	
	
	private final IOByteBuffer          bufferIn = new IODynamicByteBuffer().charset(UTF_8);
	private final IOByteBuffer          bufferOut = new IODynamicByteBuffer().charset(UTF_8);
	private volatile boolean            closed = false;
	private volatile boolean            connected = false;
	private IOSocketChannel             channel = null;
	private long                        heartbeat = -1;
	private Set<BARETransactionModel>   transactionModels = null;
	private final List<BARETransaction> transactions = new ArrayList<>();
	
	
	
	protected BAREConnection (){
		super();
	}
	
	
	
	final void clearTransactions (){
		this.transactions.clear();
	}
	
	public final void close (){
		try {
			this.channel.close();
		} finally {
			this.closed = true;
		}
	}
	
	final void dispatch (final BARETransaction transaction) throws IOException {
		try {
			transaction.call();
			
			transaction.complete();
			
			this.bufferOut.rewind();
			this.bufferOut.putObject(transaction.getResponse());
			this.bufferOut.head();
			
			this.bufferOut.write(this.channel);
			this.bufferOut.compact();
		} catch (final JSOXException ex) {
			this.onError(ex);
		}
	}
	
	final boolean dispatchAll () throws IOException {
		final Iterator<BARETransaction> it;
		
		it = this.transactions.iterator();
		
		if (! it.hasNext()) 
			return false;
		
		while (it.hasNext()) {
			final BARETransaction transaction;
			
			transaction = it.next();
			
			try {
				dispatch(transaction);
			} finally {
				it.remove();
			}
		}
		
		return true;
	}
	
	final void finishConnect () throws IOException {
		this.channel.wrappedChannel().finishConnect();
	}
	
	private BARETransactionModel getTransactionModel (final String control){
		for (final var transactionModel : this.transactionModels) {
			if (transactionModel.control().equalsIgnoreCase(control)) {
				return transactionModel;
			}
		}
		
		return null;
	}
	
	@BAREControl("heartbeat")
	private void handleHeartbeat (final BAREWireTestRequest request, final BAREWireTestResponse response){
		this.heartbeat = System.currentTimeMillis();
	}
	
	final void initializeChannel (final SocketChannel channel){
		this.channel = IOSocketChannel.wrap(channel);
	}
	
	final void initializeTransactionModels (final Set<BARETransactionModel> transactionModels){
		this.transactionModels = transactionModels; 
	}
	
	public final boolean isClosed (){
		return this.closed;
	}
	
	public final boolean isConnected (){
		return this.connected;
	}
	
	public final boolean isSuspect (){
		return ((System.currentTimeMillis() - this.heartbeat) >= TIMEOUT_HEARTBEAT);
	}
	
	boolean listen () throws IOException {
		final long                 bytesRead;
		final BARETransactRequest  packet;
		final BARETransaction      transaction;
		final BARETransactionModel transactionModel;
		
		bufferIn.head();
		
		bytesRead = this.channel.read(bufferIn);
		
		if (bytesRead == 0) 
			return false;
		
		try {
			bufferIn.rewind();
			
			packet = bufferIn.getObject(BARETransactRequest.class);
			
			bufferIn.compact();
			
			transactionModel = getTransactionModel(packet.control);
			
			if (null == transactionModel) {
				throw new Error();
			}
			
			transaction = transactionModel.newTransaction(this, packet);
			transaction.prepare();
			
			transactions.add(transaction);
			
			return true;
		} catch (final IncompleteObjectException ex) {
			Exceptions.ignore(ex);
			
			return false;
		} catch (final JSOXException ex) {
			this.onError(ex);
			
			return false;
		}
	}
	
	final void markConnected (final boolean connected){
		final boolean wasConnected;
		
		wasConnected = this.connected;
		
		if (connected != wasConnected) {
			if (this.connected = connected) {
				this.onConnect();
				
				this.heartbeat = System.currentTimeMillis();
			} else {
				this.onDisconnect();
			}
		}
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
	
	public final void reset (){
		this.channel.close();
	}
}