/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.net.sock.model.bare;

import com.xahico.boot.lang.jsox.JSOXVariant;
import java.util.UUID;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class BARETransaction {
	private final BARETransactRequest  requestWrapper;
	private final BARETransactResponse responseWrapper;
	
	private BARECallback               callback = null;
	private final String               id;
	private final Object               instance;
	private final BARETransactionModel model;
	private Status                     status = Status.INITIAL;
	
	
	
	BARETransaction (final Object instance, final BARETransactionModel model){
		super();
		
		this.id = UUID.randomUUID().toString();
		this.instance = instance;
		this.model = model;
		this.requestWrapper = new BARETransactRequest();
		this.requestWrapper.control = model.control();
		this.requestWrapper.request = new JSOXVariant();
		this.requestWrapper.transactionId = this.id;
		this.responseWrapper = new BARETransactResponse();
		this.responseWrapper.response = new JSOXVariant();
	}
	
	BARETransaction (final Object instance, final BARETransactionModel model, final BARETransactRequest requestWrapper){
		super();
		
		this.id = requestWrapper.transactionId;
		this.instance = instance;
		this.model = model;
		this.requestWrapper = requestWrapper;
		this.responseWrapper = new BARETransactResponse();
		this.responseWrapper.response = new JSOXVariant();
		this.responseWrapper.transactionId = requestWrapper.transactionId;
	}
	
	
	
	public boolean isUnhandled (){
		return (this.status == Status.INITIAL);
	}
	
	
	
	
	void call (){
		final BARERequest  request;
		final BAREResponse response;
		
		request = this.model.createRequest();
		request.assume(this.requestWrapper.request);
		
		response = this.model.createResponse();
		response.assume(this.responseWrapper.response);
		
		this.model.call(instance, request, response);
		
		requestWrapper.request.assume(request);
		
		responseWrapper.response.assume(response);
	}
	
	void complete (){
		this.status = Status.COMPLETE;
	}
	
	public BARECallback getCallback (){
		return this.callback;
	}
	
	public String getId (){
		return this.id;
	}
	
	BARETransactRequest getRequest (){
		return this.requestWrapper;
	}
	
	BARETransactResponse getResponse (){
		return this.responseWrapper;
	}
	
	void onRequest (){
		final BARERequest request;
		
		request = this.model.createRequest();
		request.assume(this.requestWrapper.request);
		
		this.callback.onRequest(request);
		
		requestWrapper.request.assume(request);
	}
	
	void onResponse (){
		final BAREResponse response;
		
		response = this.model.createResponse();
		response.assume(this.responseWrapper.response);
		
		this.callback.onResponse(response);
		
		responseWrapper.response.assume(response);
	}
	
	void prepare (){
		this.status = Status.PENDING;
	}
	
	public void setCallback (final BARECallback callback){
		this.callback = callback;
	}
	
	public Status status (){
		return this.status;
	}
	
	
	
	public static enum Status {
		COMPLETE,
		
		INITIAL,
		
		PENDING,
	}
}