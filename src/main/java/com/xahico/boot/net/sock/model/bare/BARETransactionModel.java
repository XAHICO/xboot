/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.net.sock.model.bare;

import com.xahico.boot.lang.jsox.JSOXObject;
import java.util.concurrent.ExecutionException;

/**
 * TBD.
 * 
 * @param <TREQ> 
 * TBD.
 * 
 * @param <TRSP> 
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
final class BARETransactionModel <TREQ extends BARERequest, TRSP extends BAREResponse> {
	private final String     control;
	private final BAREMethod method;
	
	
	
	BARETransactionModel (final String control, final BAREMethod method){
		super();
		
		this.control = control;
		this.method = method;
	}
	
	
	
	public void call (final Object instance, final BARERequest request, final BAREResponse response){
		try {
			this.method.invoke(instance, request, response);
		} catch (final ExecutionException ex) {
			throw new Error(ex);
		}
	}
	
	public String control (){
		return this.control;
	}
	
	BARERequest createRequest (){
		return (BARERequest) JSOXObject.newInstanceOf(method.getRequestClass());
	}
	
	BAREResponse createResponse (){
		return (BAREResponse) JSOXObject.newInstanceOf(method.getResponseClass());
	}
	
	public BARETransaction newTransaction (final Object instance){
		return new BARETransaction(instance, this);
	}
	
	public BARETransaction newTransaction (final Object instance, final BARETransactRequest request){
		return new BARETransaction(instance, this, request);
	}
}