/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.publish.handlers;

import com.xahico.boot.publish.GWXServiceConfiguration;
import com.xahico.boot.publish.GWXServiceContext;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class GWXWebSocketForceInsecureUpgradeHandler extends GWXHandler<FullHttpRequest> {
	public GWXWebSocketForceInsecureUpgradeHandler (final GWXServiceContext context, final GWXServiceConfiguration config){
		super(context, config);
	}
	
	

	@Override
	protected void channelRead0 (final ChannelHandlerContext ctx, final FullHttpRequest request){
		final HttpResponse response;

			System.out.println("@WebSocketForceInsecureUpgradeHandler");
		if (! "websocket".equalsIgnoreCase(request.headers().get(HttpHeaderNames.UPGRADE))) {
			ctx.fireChannelRead(request.retain());

			return;
		}

		response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.UPGRADE_REQUIRED);
		response.headers().set(HttpHeaderNames.SEC_WEBSOCKET_VERSION, "13");
		response.headers().set(HttpHeaderNames.CONTENT_LENGTH, 0);

		ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
	}
}