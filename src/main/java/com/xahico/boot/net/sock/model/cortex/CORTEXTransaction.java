/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.net.sock.model.cortex;

import com.xahico.boot.util.Exceptions;
import java.util.concurrent.ExecutionException;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class CORTEXTransaction {
	private Callback<Void>       cbDiscard = null;
	private Callback             cbRequest = null;
	private Callback             cbResponse = null;
	private final int            id;
	private final CORTEXMethod   method;
	private final CORTEXRequest  request;
	private final CORTEXResponse response;
	private boolean              stateRequestSent = false;
	private boolean              stateResponseReceived = false;
	
	
	
	CORTEXTransaction (final int id, final CORTEXMethod method, final CORTEXRequest request, final CORTEXResponse response){
		super();
		
		this.id = id;
		this.method = method;
		this.request = request;
		this.response = response;
	}
	
	
	
	void complete (final CORTEXSession session){
		try {
			this.method.invoke(session, request, response);
		} catch (final ExecutionException ex) {
			Exceptions.ignore(ex);
		}
	}
	
	String getControl (){
		return this.method.getControl();
	}
	
	int getId (){
		return this.id;
	}
	
	CORTEXRequest getRequest (){
		return this.request;
	}
	
	CORTEXResponse getResponse (){
		return this.response;
	}
	
	void invokeDiscard (){
		this.cbDiscard.invoke(null);
	}
	
	void invokeRequest (){
		this.cbRequest.invoke((CORTEXRequest)this.request);
	}
	
	void invokeResponse (){
		this.cbResponse.invoke(this.response);
	}
	
	boolean isCompleted (){
		return (this.isRequestSent() && this.isResponseReceived());
	}
	
	boolean isRequestSent (){
		return this.stateRequestSent;
	}
	
	boolean isResponseReceived (){
		return this.stateResponseReceived;
	}
	
	void markRequestSent (){
		this.stateRequestSent = true;
	}
	
	void markResponseReceived (){
		this.stateResponseReceived = true;
	}
	
	public void onDiscard (final Callback<Void> callback){
		this.cbDiscard = callback;
	}
	
	public <T extends CORTEXRequest> void onRequest (final Callback<T> callback){
		this.cbRequest = callback;
	}
	
	public <T extends CORTEXResponse> void onResponse (final Callback<T> callback){
		this.cbResponse = callback;
	}
	
	public boolean validate (){
		return ((null != this.cbDiscard) && (null != this.cbRequest) && (null != this.cbResponse));
	}
	
	
	
	@FunctionalInterface
	public static interface Callback <T> {
		void invoke (final T object);
	}
}