/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.publish.handlers;

import com.xahico.boot.pilot.Core;
import com.xahico.boot.publish.GWXAPIInterface;
import com.xahico.boot.publish.GWXEventListener;
import com.xahico.boot.publish.GWXInvalidRouteException;
import com.xahico.boot.publish.GWXPermission;
import com.xahico.boot.publish.GWXRoute;
import com.xahico.boot.publish.GWXServiceConfiguration;
import com.xahico.boot.publish.GWXServiceContext;
import com.xahico.boot.publish.GWXSession;
import com.xahico.boot.publish.GWXSupportedMimeType;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import java.net.URLDecoder;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class GWXSseEventHandler extends GWXHandler<FullHttpRequest> {
	public GWXSseEventHandler (final GWXServiceContext context, final GWXServiceConfiguration config){
		super(context, config);
	}
	
	
	
	@Override
	protected void channelRead0 (final ChannelHandlerContext ctx, final FullHttpRequest request){
		final GWXRoute   route;
		final GWXSession session;
		final long       whenBegin;

		whenBegin = System.currentTimeMillis();

		if (! GWXSupportedMimeType.SSE.toString().equals(request.headers().get(HttpHeaderNames.ACCEPT))) {
			ctx.fireChannelRead(request.retain());

			return;
		}

		try {
			route = GWXRoute.parseFullString(URLDecoder.decode(request.uri(), config.charset), ".");
		} catch (final GWXInvalidRouteException ex) {
			ex.printStackTrace();

			ctx.fireChannelRead(request.retain());

			return;
		}

		session = ctx.channel().attr(context.keySession).get();

		if (null == session) {
			ctx.fireChannelRead(request.retain());

			return;
		}

		Core.executeBlocking(() -> {
			final GWXAPIInterface.Handle interfaceHandle;

			if (! session.checkAccess(route.path.withoutExtension(), GWXPermission.OBSERVE)) {
				ctx.channel().eventLoop().execute(() -> {
					final HttpResponse response;

					response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.FORBIDDEN);

					completeResponse(request, response);

					ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
				});

				return;
			}

			interfaceHandle = session.getInterface(route.version, () -> {
				return context.interfaceManager.requireInterface(route.version);
			});

			if (null == interfaceHandle) {
				ctx.channel().eventLoop().execute(() -> {
					final HttpResponse response;

					response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND);

					completeResponse(request, response);

					ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
				});

				return;
			}

			Core.executeAsync(() -> {
				final GWXEventListener listener;
				
				listener = new GWXEventListener(context.rcm, ctx.channel(), GWXEventListener.Type.SSE, interfaceHandle, route.path);
				listener.attach(session);

				ctx.channel().attr(context.keyListener).set(listener);

				ctx.channel().eventLoop().execute(() -> {
					final HttpResponse response;
					final long         whenEnd;

					response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);

					completeResponse(request, response);

					response.headers()
						  .set(HttpHeaderNames.CONTENT_TYPE, GWXSupportedMimeType.SSE.toString())
						  .set(HttpHeaderNames.CACHE_CONTROL, "no-cache")
						  .set(HttpHeaderNames.CONNECTION, "keep-alive");

					ctx.write(response);

					listener.ping();

					session.attachEventListener(listener);

					System.out.println("Created SSE Event Handler");

					whenEnd = System.currentTimeMillis();

					System.out.println("@SseEventHandler took %d millisecond(s)".formatted(whenEnd - whenBegin));
				});
			});
		});
	}

	private void completeResponse (final HttpRequest request, final HttpResponse response){
		final String origin;

		origin = request.headers().get(HttpHeaderNames.ORIGIN);

		if (null != origin) {
			response.headers()
				  .set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN, origin)
				  .set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true")
				  .set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_METHODS, "GET,HEAD,OPTIONS,POST,PUT")
				  .set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_HEADERS, "Origin,Accept,X-Requested-With,Content-Type,Authorization");
		}
	}
}