/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.net.sock.model.asio;

import com.xahico.boot.lang.jsox.JSOXException;
import com.xahico.boot.lang.jsox.JSOXObject;
import com.xahico.boot.lang.jsox.JSOXVariant;
import com.xahico.boot.reflection.MethodNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public class ASIOSession extends ASIOExchange {
	private final Set<ASIOMethod>       controls = ASIOUtilities.getControlMethods(this.getClass());
	private final Set<ASIOMethod>       handlers = ASIOUtilities.getHandlerMethods(this.getClass());
	private final List<ASIOTransaction> transactions = Collections.synchronizedList(new ArrayList<>());
	
	
	
	protected ASIOSession (){
		super();
	}
	
	
	
	@Override
	void destroy (){
		synchronized (this.transactions) {
			for (final var transaction : this.transactions) {
				transaction.end(this);
			}
		}
	}
	
	private ASIOMethod getControlMethod (final String control) throws MethodNotFoundException {
		for (final var method : this.controls) {
			if (method.getControl().equalsIgnoreCase(control)) {
				return method;
			}
		}
		
		throw new MethodNotFoundException(String.format("No method found for '%s'", control));
	}
	
	private ASIOMethod getHandlerMethod (final String control) throws MethodNotFoundException {
		for (final var method : this.handlers) {
			if (method.getControl().equalsIgnoreCase(control)) {
				return method;
			}
		}
		
		throw new MethodNotFoundException(String.format("No method found for '%s'", control));
	}
	
	private ASIOTransaction getTransaction (final int transactionId){
		synchronized (this.transactions) {
			final Iterator<ASIOTransaction> it;
			
			it = this.transactions.iterator();
			
			while (it.hasNext()) {
				final ASIOTransaction transaction;
				
				transaction = it.next();
				
				if (transaction.getId() == transactionId) {
					it.remove();
					
					return transaction;
				}
			}
		}
		
		return null;
	}
	
	private void handleRequest (final JSOXVariant object) throws IOException {
		try {
			final ASIOMethod              handler;
			final ASIORequest             request;
			final ASIOTransactionRequest  requestTransaction;
			final ASIOResponse            response;
			final ASIOTransactionResponse responseTransaction;
			
			requestTransaction = JSOXObject.newInstanceOf(ASIOTransactionRequest.class, object);
			
			handler = this.getHandlerMethod(requestTransaction.control);
			
			request = (ASIORequest) JSOXObject.newInstanceOf(handler.getRequestClass());
			request.assume(requestTransaction.data);
			
			response = (ASIOResponse) JSOXObject.newInstanceOf(handler.getResponseClass());
			
			handler.invoke(this, request, response);
			
			responseTransaction = new ASIOTransactionResponse();
			responseTransaction.data = response.toVariant();
			responseTransaction.tid = requestTransaction.tid;
			
			this.post(ASIOMessage.wrapObject(responseTransaction.toVariant(), UTF_8));
		} catch (final ExecutionException | JSOXException | MethodNotFoundException ex) {
			throw new Error(ex);
		}
	}
	
	private void handleResponse (final JSOXVariant object) throws IOException {
		final ASIOTransaction         transaction;
		final ASIOTransactionResponse transactionResponse;
		
		transactionResponse = JSOXObject.newInstanceOf(ASIOTransactionResponse.class, object);
		
		transaction = this.getTransaction(transactionResponse.tid);
		
		if (null != transaction) try {
			transaction.getResponse().assume(transactionResponse.data);
			
			transaction.end(this);
		} finally {
			transaction.complete(this);
		}
	}
	
	@Override
	protected final void onMessage (final byte[] buffer, final int available) throws IOException {
		final JSOXVariant object;
		final String      objectData;
		
		objectData = new String(Arrays.copyOfRange(buffer, 0, available), StandardCharsets.UTF_8);
		
		object = new JSOXVariant(objectData);
		
		if (object.has("control")) {
			handleRequest(object);
		} else /*if (object.has("status"))*/ {
			handleResponse(object);
		/*} else {
			throw new Error(objectData);
		*/}
	}
	
	public final <REQ extends ASIORequest, RSP extends ASIOResponse> boolean transact (final String control, final ASIOTransactionHandler<REQ, RSP> handler){
		try {
			final ASIOMethod             method;
			final ASIOTransaction        transaction;
			final ASIOTransactionRequest transactionRequest;
			
			if (this.isDisconnected()) {
				return false;
			}
			
			method = this.getControlMethod(control);
			
			transaction = new ASIOTransaction(this.transactions.size(), method, handler);
			transaction.begin(this);
			
			this.transactions.add(transaction);
			
			transactionRequest = new ASIOTransactionRequest();
			transactionRequest.control = control;
			transactionRequest.data = transaction.getRequest().toVariant();
			transactionRequest.tid = transaction.getId();
			
			this.post(ASIOMessage.wrapObject(transactionRequest.toVariant(), UTF_8));
			
			return true;
		} catch (final JSOXException | MethodNotFoundException ex) {
			throw new Error(ex);
		}
	}
}