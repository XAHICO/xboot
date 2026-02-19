/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.publish;

import com.xahico.boot.lang.jsox.JSOXUpdateHandler;
import com.xahico.boot.lang.jsox.JSOXVariant;
import java.util.HashSet;
import java.util.Set;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public class GWXTransactionAdapter implements GWXTransactionHandler {
	private final GWXTransactionCallbacks        callbacks;
	private JSOXVariant                          data = null;
	private final JSOXUpdateHandler<JSOXVariant> factory;
	private Set<JSOXVariant>                     returns = null;
	
	
	
	public GWXTransactionAdapter (final JSOXUpdateHandler<JSOXVariant> factory, final GWXTransactionCallbacks callbacks){
		super();
		
		this.factory = factory;
		this.callbacks = callbacks;
	}
	
	
	
	@Override
	public void onError (final Throwable cause){
		this.callbacks.onFailure((null != cause) ? cause.getMessage() : null);
	}
	
	@Override
	public void onFinalize (final boolean status){
		if (status) {
			this.callbacks.onSuccess(this.data, this.returns);
		}
	}
	
	@Override
	public void onRequest (final JSOXVariant request){
		this.factory.handleUpdate(request);
	}
	
	@Override
	public void onResponse (final JSOXVariant response, final boolean hasReturns){
		this.callbacks.onComplete();
		
		if (response.has("error")) {
			final String error;
			
			error = response.getString("error");
			
			this.callbacks.onFailure(error);
			
			return;
		}
		
		if (response.has("status")) {
			final String status;
			
			status = response.getString("status");
			
			if (! status.equalsIgnoreCase("SUCCESS")) {
				this.callbacks.onFailure(status);
			} else {
				this.data = response;
			}
			
			return;
		}
	}
	
	@Override
	public void onStream (final JSOXVariant object){
		this.returns.add(object);
	}
	
	@Override
	public void onStreamClose (){
		
	}
	
	@Override
	public void onStreamOpen (){
		this.returns = new HashSet<>();
	}
}