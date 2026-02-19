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
public final class GWXHttpInsecureRedirectHandler extends GWXHandler<FullHttpRequest> {
	public GWXHttpInsecureRedirectHandler (final GWXServiceContext context, final GWXServiceConfiguration config){
		super(context, config);
	}
	
	
	
	@Override
	protected void channelRead0 (final ChannelHandlerContext ctx, final FullHttpRequest request){
		final String       host;
		final String       location;
		final HttpResponse response;

		System.out.println("@HttpInsecureRedirectHandler");
		
		host = request.headers().get(HttpHeaderNames.HOST);

		location = ("https://" + host + request.uri());

		response = new DefaultFullHttpResponse(
			HttpVersion.HTTP_1_1, HttpResponseStatus.MOVED_PERMANENTLY);

		response.headers().set(HttpHeaderNames.LOCATION, location);
		response.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, 0);

		ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
	}
}