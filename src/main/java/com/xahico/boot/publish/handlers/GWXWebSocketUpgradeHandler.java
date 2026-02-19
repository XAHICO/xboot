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
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import java.net.URLDecoder;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class GWXWebSocketUpgradeHandler extends GWXHandler<FullHttpRequest> {
	public GWXWebSocketUpgradeHandler (final GWXServiceContext context, final GWXServiceConfiguration config){
		super(context, config);
	}
	
	
	
	@Override
	protected void channelRead0 (final ChannelHandlerContext ctx, final FullHttpRequest request){
		final GWXRoute   route;
		final GWXSession session;
		final long       whenBegin;

		whenBegin = System.currentTimeMillis();

		if (! "websocket".equalsIgnoreCase(request.headers().get(HttpHeaderNames.UPGRADE))) {
			ctx.fireChannelRead(request.retain());

			return;
		}

		try {
			route = GWXRoute.parseFullString(URLDecoder.decode(request.uri(), config.charset), ".");
		} catch (final GWXInvalidRouteException ex) {
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

					ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
				});

				return;
			}

			Core.executeAsync(() -> {
				final GWXEventListener listener;

				listener = new GWXEventListener(context.rcm, ctx.channel(), GWXEventListener.Type.WS, interfaceHandle, route.path);
				listener.attach(session);

				ctx.channel().attr(context.keyListener).set(listener);

				ctx.channel().eventLoop().execute(() -> {
					final long                             whenEnd;
					final WebSocketServerHandshakerFactory wsFactory;
					final WebSocketServerHandshaker        wsHandshaker;
					final String                           wsURL;

					wsURL = request.uri(); //"%s://0.0.0.0:%d/v%d/".formatted(wsProtocol, port, route.version);

					wsFactory = new WebSocketServerHandshakerFactory(wsURL, null, true);
					wsHandshaker = wsFactory.newHandshaker(request);

					if (wsHandshaker == null) {
						WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
					} else {
						wsHandshaker.handshake(ctx.channel(), request);
					}

					session.attachEventListener(listener);

					listener.ping();

					whenEnd = System.currentTimeMillis();

					System.out.println("@WebSocketUpgradeHandler took %d millisecond(s)".formatted(whenEnd - whenBegin));
				});
			});
		});
	}
}