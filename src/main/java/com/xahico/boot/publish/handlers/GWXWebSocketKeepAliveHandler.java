/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.publish.handlers;

import com.xahico.boot.publish.GWXServiceConfiguration;
import com.xahico.boot.publish.GWXServiceContext;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class GWXWebSocketKeepAliveHandler extends GWXHandler<WebSocketFrame> {
	public GWXWebSocketKeepAliveHandler (final GWXServiceContext context, final GWXServiceConfiguration config){
		super(context, config);
	}
	
	
	
	protected void channelRead0 (final ChannelHandlerContext ctx, final WebSocketFrame frame) {
			System.out.println("@WebSocketKeepAliveHandler");
		if (frame instanceof PongWebSocketFrame) {
			// pong received, can log or ignore
		} else {
			// pass through other frames
			ctx.fireChannelRead(frame.retain());
		}
	}

	@Override
	public void exceptionCaught (final ChannelHandlerContext ctx, final Throwable cause){
	    cause.printStackTrace();

	    ctx.close();
	}
}