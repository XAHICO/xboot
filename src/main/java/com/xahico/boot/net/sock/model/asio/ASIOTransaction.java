/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.xahico.boot.net.sock.model.asio;

import com.xahico.boot.lang.jsox.JSOXObject;
import java.util.concurrent.ExecutionException;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
final class ASIOTransaction {
	private final ASIOTransactionHandler handler;
	private final int                    id;
	private final ASIOMethod             method;
	private final ASIORequest            request;
	private final ASIOResponse           response;
	
	
	
	ASIOTransaction (final int id, final ASIOMethod method, final ASIOTransactionHandler handler){
		super();
		
		this.id = id;
		this.method = method;
		this.request = (ASIORequest) JSOXObject.newInstanceOf(method.getRequestClass());
		this.response = (ASIOResponse) JSOXObject.newInstanceOf(method.getResponseClass());
		this.handler = handler;
	}
	
	
	
	public void begin (final ASIOSession session){
		this.handler.onRequest(this.request);
	}
	
	public void complete (final ASIOSession session){
		try {
			this.method.invoke(session, this.request, this.response);
		} catch (final ExecutionException ex) {
			throw new Error(ex);
		}
	}
	
	public void end (final ASIOSession session){
		this.handler.onResponse(this.response);
	}
	
	public int getId (){
		return this.id;
	}
	
	public ASIORequest getRequest (){
		return this.request;
	}
	
	public ASIOResponse getResponse (){
		return this.response;
	}
}