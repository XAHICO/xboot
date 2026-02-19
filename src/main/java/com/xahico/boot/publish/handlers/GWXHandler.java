/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.publish.handlers;

import com.xahico.boot.publish.GWXServiceConfiguration;
import com.xahico.boot.publish.GWXServiceContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public abstract class GWXHandler <T> extends SimpleChannelInboundHandler<T> {
	protected final GWXServiceConfiguration config;
	protected final GWXServiceContext       context;
	
	
	
	GWXHandler (final GWXServiceContext context, final GWXServiceConfiguration config){
		super();
		
		this.context = context;
		this.config = config;
	}
}