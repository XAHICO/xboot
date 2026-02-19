/*
 * Written by Tuomas Kontiainen <https://www.github.com/tutomiko>
 * 
 * Copyright (c) 2023, Tuomas Kontiainen
 */
package com.xahico.boot.net.web.http.model.glass;

import com.xahico.boot.reflection.ReflectionMethod;
import java.util.concurrent.ExecutionException;

/**
 * TBD.
 * 
 * @param <REQ> 
 * TBD.
 * 
 * @param <RSP> 
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
final class GlassControlMethod <REQ extends GlassRequest, RSP extends GlassResponse> {
	private final String           control;
	private final ReflectionMethod methodHandle;
	private final Class<REQ>       requestClass;
	private final Class<RSP>       responseClass;
	
	
	
	GlassControlMethod (final ReflectionMethod methodHandle, final String control, final Class<REQ> requestClass, final Class<RSP> responseClass){
		super();
		
		this.methodHandle = methodHandle;
		this.control = control;
		this.requestClass = requestClass;
		this.responseClass = responseClass;
	}
	
	
	
	public GlassCallback[] getCallbacks (){
		final GlassCallback  callback;
		final GlassCallbacks callbacks;
		
		callbacks = this.methodHandle.getAnnotation(GlassCallbacks.class);
		
		if (null != callbacks) 
			return callbacks.value();
		
		callback = this.methodHandle.getAnnotation(GlassCallback.class);
		
		if (null != callback) 
			return new GlassCallback[]{callback};
		
		return new GlassCallback[0];
	}
	
	public String getControl (){
		return this.control;
	}
	
	public Class<REQ> getRequestClass (){
		return this.requestClass;
	}
	
	public Class<RSP> getResponseClass (){
		return this.responseClass;
	}
	
	public void invoke (final Object instance, final REQ request, final RSP response) throws ExecutionException {
		this.methodHandle.invoke(instance, request, response);
	}
}