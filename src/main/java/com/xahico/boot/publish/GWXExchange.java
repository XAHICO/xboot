/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.publish;

import com.xahico.boot.lang.jsox.JSOXVariant;
import io.netty.channel.ChannelHandlerContext;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class GWXExchange {
	private volatile GWXObject          results = null;
	private volatile GWXException       error = null;
	
	private volatile boolean            callable = false;
	private ReadyHandler                callback = (results, error) -> {};
	private final ChannelHandlerContext ctx;
	public final String                 method;
	private volatile boolean            ready = false;
	public final JSOXVariant            request = new JSOXVariant();
	public final JSOXVariant            response = new JSOXVariant();
	
	
	
	public GWXExchange (final ChannelHandlerContext ctx, final String method){
		super();
		
		this.ctx = ctx;
		this.method = method;
	}
	
	
	
	public void ready (final GWXObject results, final GWXException error){
		if (this.callable) {
			this.ready = true;
			
			this.ctx.executor().execute(() -> this.callback.call(results, error));
		} else {
			this.error = error;
			this.results = results;
			this.ready = true;
		}
	}
	
	public void ready (final ReadyHandler callback){
		this.callback = callback;
		this.callable = true;
		
		if (this.ready) {
			this.callback.call(this.results, this.error);
		}
	}
	
	
	
	@FunctionalInterface
	public static interface ReadyHandler {
		void call (final GWXObject results, final GWXException error);
	}
}