/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.net.web.http.model.glass;

import com.xahico.boot.util.StringUtilities;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public enum GlassCallbackMethod {
	/**
	 * Delete the authentication token cookie.
	**/
	DELETE_AUTH((target) -> {
		return "%s();".formatted(GlassInterface.GF_AUTH_CLEAR);
	}),
	
	/**
	 * Execute arbitrary JavaScript code.
	**/
	EXECUTE((target) -> {
		return target;
	}),
	
	/**
	 * Re-direct to another site.
	**/
	REDIRECT((target) -> {
		return "__window__.travelTo(`%s`);".formatted(StringUtilities.isQuoted(target) ? StringUtilities.unquote(target) : target);
//		return "__window__.travelTo(%s);".formatted((target.startsWith("/") ? StringUtilities.quote(target) : target));
	}),
	
	/**
	 * Store the authentication token cookie.
	**/
	STORE_AUTH((target) -> {
		return "%s(%s);".formatted(GlassInterface.GF_AUTH_INIT, "response.token");
	});
	
	
	
	private final Handler handler;
	
	
	
	GlassCallbackMethod (final Handler handler){
		this.handler = handler;
	}
	
	
	
	public String embed (final String target){
		return this.handler.call(target);
	}
	
	
	
	@FunctionalInterface
	private static interface Handler {
		String call (final String target);
	}
}