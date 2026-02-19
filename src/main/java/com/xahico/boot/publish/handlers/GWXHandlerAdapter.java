/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.publish.handlers;

import com.xahico.boot.publish.GWXServiceConfiguration;
import com.xahico.boot.publish.GWXServiceContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public abstract class GWXHandlerAdapter extends ChannelInboundHandlerAdapter {
	protected final GWXServiceConfiguration config;
	protected final GWXServiceContext       context;
	
	
	
	GWXHandlerAdapter (final GWXServiceContext context, final GWXServiceConfiguration config){
		super();
		
		this.context = context;
		this.config = config;
	}
}