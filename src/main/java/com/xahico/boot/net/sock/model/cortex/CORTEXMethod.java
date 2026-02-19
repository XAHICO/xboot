/*
 * Written by Tuomas Kontiainen <https://www.github.com/tutomiko>
 * 
 * Copyright (c) 2023, Tuomas Kontiainen
 */
package com.xahico.boot.net.sock.model.cortex;

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
final class CORTEXMethod <REQ extends CORTEXRequest, RSP extends CORTEXResponse> {
	private final String           control;
	private final ReflectionMethod methodHandle;
	private final Class<REQ>       requestClass;
	private final Class<RSP>       responseClass;
	
	
	
	CORTEXMethod (final ReflectionMethod methodHandle, final String control, final Class<REQ> requestClass, final Class<RSP> responseClass){
		super();
		
		this.methodHandle = methodHandle;
		this.control = control;
		this.requestClass = requestClass;
		this.responseClass = responseClass;
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