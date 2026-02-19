/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.publish.handlers;

import com.xahico.boot.publish.GWXEventListener;
import com.xahico.boot.publish.GWXServiceConfiguration;
import com.xahico.boot.publish.GWXServiceContext;
import com.xahico.boot.publish.GWXSession;
import com.xahico.boot.publish.GWXUtilities;
import com.xahico.boot.util.CollectionUtilities;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.util.AttributeKey;
import java.util.Set;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class GWXAuthHandler extends GWXHandler<FullHttpRequest> {
	private final AttributeKey<String> keyAuth = AttributeKey.valueOf("auth");
	
	
	
	public GWXAuthHandler (final GWXServiceContext context, final GWXServiceConfiguration config){
		super(context, config);
	}
	
	
	
	@Override
	protected void channelRead0 (final ChannelHandlerContext ctx, final FullHttpRequest request){
		final GWXSession session;
		final String     token;
		final String     tokenCookieName;
		boolean          tokenFromCookie = false;
		final String     tokenHeader;

		tokenCookieName = GWXUtilities.buildTokenIdentity(context.instanceFactory.getProductionClass());

		tokenHeader = request.headers().get("Authorization");

		if (null != tokenHeader) {
			if ((tokenHeader.length() <= 7) || !tokenHeader.startsWith("Bearer ")) {
				token = null;
			} else {
				token = tokenHeader.substring(7);
			}
		} else if (null != config.webRoot) {
			final String cookieHeader;

			cookieHeader = request.headers().get(HttpHeaderNames.COOKIE);

			if (null != cookieHeader) {
				final Cookie      cookie;
				final Set<Cookie> cookies;

				cookies = ServerCookieDecoder.STRICT.decode(cookieHeader);

				cookie = CollectionUtilities.seek(cookies, (__) -> __.name().equals(tokenCookieName), false, null);

				if (null != cookie) {
					token = cookie.value();

					tokenFromCookie = true;
				} else {
					token = null;
				}
			} else {
				token = null;
			}
		} else {
			token = null;
		}

		if (null == token) {
			session = createInstance();

			ctx.channel().attr(context.keySession).set(session);
		} else {
			ctx.channel().attr(keyAuth).set(token);

			session = context.sessions.get(token);

			if (null != session) {
				session.updateLastContact();

				ctx.channel().attr(context.keySession).set(session);
			}
		}

		if (null == session) {
			final HttpResponse response;

			response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.UNAUTHORIZED);

			if (tokenFromCookie) {
				response.headers().set(HttpHeaderNames.SET_COOKIE, "%s=; Max-Age=0; Path=/; HttpOnly; Secure; SameSite=None".formatted(tokenCookieName));
			}

			completeResponse(request, response);

			ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
		} else {
			ctx.channel().attr(context.keySession).set(session);

			ctx.channel().attr(keyAuth).set(token);

			ctx.fireChannelRead(request.retain());
		}
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
	
	private GWXSession createInstance (){
		final GWXSession session;
		
		session = this.context.instanceFactory.newInstance();
		session.assignCallbacks(new GWXSession.Callbacks() {
			@Override
			public void onAuthenticate (final GWXSession session){
				//System.out.println("created auth session for '" + session.getToken() + "'");
				context.sessions.put(session.getToken(), session);
			}
			
			@Override
			public void onAuthenticateReset (final GWXSession session){
				//System.out.println("reset auth for "+session.getToken());
				context.sessions.remove(session.getToken());
			}
			
			@Override
			public void onDestroy (final GWXSession session){
				if (null != session.getToken()) {
					context.sessions.remove(session.getToken());
				}
			}
			
			@Override
			public void onEventListenerConnect (final GWXSession session, final GWXEventListener listener){
				System.out.println("Connected event listener to session " + session);
				
				listener.attach(session);
			}
			
			@Override
			public void onEventListenerDisconnect (final GWXSession session, final GWXEventListener listener){
				System.out.println("Disconnected event listener from session " + session);
				
				listener.detach();
			}
		});
		session.attachExecutor(context.executor);
		session.setRetainEvents(config.retainEvents);
		session.setRetainHandles(config.retainHandles);
		session.initLocals(context.rcm);
		session.initNamespace(context.webNamespace);
		session.timeout(context.sessionTimeout);
		session.create();
		
		return session;
	}
}