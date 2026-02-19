/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.publish.handlers;

import com.xahico.boot.lang.jsox.JSOXVariant;
import com.xahico.boot.pilot.Core;
import com.xahico.boot.publish.GWXAPIInterface;
import com.xahico.boot.publish.GWXContext;
import com.xahico.boot.publish.GWXException;
import com.xahico.boot.publish.GWXExchange;
import com.xahico.boot.publish.GWXInstance;
import com.xahico.boot.publish.GWXInvalidRouteException;
import com.xahico.boot.publish.GWXNode;
import com.xahico.boot.publish.GWXNodeCollection;
import com.xahico.boot.publish.GWXObject;
import com.xahico.boot.publish.GWXPermission;
import com.xahico.boot.publish.GWXRoute;
import com.xahico.boot.publish.GWXServiceConfiguration;
import com.xahico.boot.publish.GWXServiceContext;
import com.xahico.boot.publish.GWXSession;
import com.xahico.boot.publish.GWXStatus;
import com.xahico.boot.publish.GWXSupportedMimeType;
import com.xahico.boot.publish.GWXUtilities;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http.QueryStringDecoder;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class GWXHttpApiHandler extends GWXHandler<FullHttpRequest> {
	private static HttpContent preparedChunk (){
		return new DefaultHttpContent(Unpooled.copiedBuffer(new byte[]{'\n'}));
	}
	
	
	
	private static final String KEYWORD_ERROR = "error";
	private static final String KEYWORD_RETURNS = "returns";
	private static final String KEYWORD_STATUS = "status";
	public static final String  PATH_BASE_API = "/api";
	
	
	
	public GWXHttpApiHandler (final GWXServiceContext callContext, final GWXServiceConfiguration config){
		super(callContext, config);
	}
	
	
	
	@Override
	protected void channelRead0 (final ChannelHandlerContext ctx, final FullHttpRequest request){
		try{
		final String     method;
		final ByteBuf    requestBuffer;
		final String     requestString;
		final GWXRoute   route;
		final GWXSession session;
		final long       whenBegin;

		whenBegin = System.currentTimeMillis();

		try {
			route = GWXRoute.parseFullString(new QueryStringDecoder(request.uri()).path(), "/");
		} catch (final GWXInvalidRouteException ex) {
			ctx.fireChannelRead(request.retain());

			return;
		}

		if (! route.root.equalsIgnoreCase(PATH_BASE_API)) {
			ctx.fireChannelRead(request.retain());

			return;
		}

		session = ctx.channel().attr(context.keySession).get();

		if (null == session) {
			ctx.fireChannelRead(request.retain());

			return;
		}

		method = request.method().name();
		
		requestBuffer = request.content();
		requestString = requestBuffer.toString(config.charset);
		
		// jump to blocking executor for interface loading (this may block)
		Core.executeBlocking(() -> {
			final GWXAPIInterface.Handle interfaceHandle;

			interfaceHandle = session.getInterface(route.version, () -> {
				return context.interfaceManager.requireInterface(route.version);
			});

			if (null == interfaceHandle) {
				ctx.channel().eventLoop().execute(() -> {
					final HttpResponse response;
					
					response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST);
					
					ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
				});
			} else {
				final GWXPermission          interfaceAccess;
				final GWXAPIInterface.Method interfaceMethod;

				interfaceAccess = GWXPermission.transformHttpMethod(method);

				interfaceMethod = interfaceHandle.lookupMethod(interfaceAccess, route.path);

				if (null == interfaceMethod) {
					if (! config.retainHandles) {
						interfaceHandle.release();
					}

					ctx.channel().eventLoop().execute(() -> {
						final HttpResponse response;

						response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.METHOD_NOT_ALLOWED);
						
						ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
					});
				} else if (interfaceMethod.authorized && !session.isAuthenticated()) {
					if (! config.retainHandles) {
						interfaceHandle.release();
					}

					ctx.channel().eventLoop().execute(() -> {
						final HttpResponse response;

						response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.UNAUTHORIZED);

						ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
					});
				} else {
					final GWXContext callContext;

					callContext = context.rcm.buildContext(session, interfaceMethod.pattern, route.path, interfaceAccess);

					if (! callContext.checkAccess(session, interfaceMethod.require)) {
						if (! config.retainHandles) {
							interfaceHandle.release();
						}

						ctx.channel().eventLoop().execute(() -> {
							final HttpResponse response;

							response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.FORBIDDEN);

							ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
						});

						return;
					}
					
					Core.executeAsync(() -> {
						final GWXExchange exchange;
						final GWXInstance instance;
						
						exchange = new GWXExchange(ctx, method);
						exchange.request.assume(GWXUtilities.parseQueryString(request.uri()));

						if (! requestString.isBlank()) {
							exchange.request.assume(requestString, false);
						}

						instance = new GWXInstance(context.rcm, interfaceHandle.version(), session);

						try {
							final GWXObject result;

							result = interfaceMethod.call(callContext, instance, exchange);

							if (! interfaceMethod.async) {
								exchange.ready(result, null);
							}
						} catch (final GWXException ex) {
							System.out.println("Request That Caused Error: " + exchange.request.toJSONString());

							exchange.ready(null, ex);
						} catch (Throwable t) {
							t.printStackTrace();
						}

						exchange.ready((result, error) -> {
							final long whenEnd;

							if (! config.retainHandles) {
								interfaceHandle.release();
							}

							if (null == error) {
								exchange.response.putString(KEYWORD_STATUS, GWXStatus.SUCCESS.name());
							} else {
								// if an error occurred then none of the response content matters
								// so we may confidently clear anything that may have been put into it
								exchange.response.clear();
								exchange.response.putString(KEYWORD_ERROR, error.getMessage());
								exchange.response.putString(KEYWORD_STATUS, error.status().name());
							}

							if (null == result) {
								ctx.channel().eventLoop().execute(() -> {
									final HttpResponse response;
									final byte[]       xhead;

									exchange.response.putInteger(KEYWORD_RETURNS, 0);

									xhead = exchange.response.toJSONStringCompact().getBytes(config.charset);

									response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);

									response.headers()
										  .set(HttpHeaderNames.CONTENT_TYPE, GWXUtilities.formatMimeType(GWXSupportedMimeType.NDJSON, config.charset))
										  .set(HttpHeaderNames.TRANSFER_ENCODING, HttpHeaderValues.CHUNKED);

									ctx.write(response);
									ctx.write(Unpooled.copiedBuffer(xhead));
									ctx.write(preparedChunk());
									ctx.flush();

									ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT).addListener(ChannelFutureListener.CLOSE);
								});
							} else if (result instanceof GWXNode serializable) {
								final byte[] xhead;
								final byte[] xbody;

								xhead = exchange.response.toJSONStringCompact().getBytes(config.charset);

								if (! session.checkAccess(serializable, GWXPermission.READ)) {
									exchange.response.putInteger(KEYWORD_RETURNS, 0);

									xbody = new JSOXVariant().toJSONStringCompact().getBytes(config.charset);
								} else {
									exchange.response.putInteger(KEYWORD_RETURNS, 1);

									xbody = serializable.serialize(false);
								}

								ctx.channel().eventLoop().execute(() -> {
									final HttpResponse response;

									response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);

									response.headers()
										  .set(HttpHeaderNames.CONTENT_TYPE, GWXUtilities.formatMimeType(GWXSupportedMimeType.NDJSON, config.charset))
										  .set(HttpHeaderNames.TRANSFER_ENCODING, HttpHeaderValues.CHUNKED);

									ctx.write(response);
									ctx.write(Unpooled.copiedBuffer(xhead));
									ctx.write(preparedChunk());
									ctx.write(Unpooled.copiedBuffer(xbody));
									ctx.write(preparedChunk());

									ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT).addListener(ChannelFutureListener.CLOSE);
								});
							} else if (result instanceof GWXNodeCollection<?> serializable) {
								final boolean requireCheckExplicitAccess;
								final byte[]  xhead;

								if (session.isPrivileged() || session.isOwnerOf(serializable) || session.checkAccess(serializable, GWXPermission.READ)) {
									requireCheckExplicitAccess = false;
								} else {
									requireCheckExplicitAccess = true;
								}

								if (! requireCheckExplicitAccess) {
									exchange.response.putInteger(KEYWORD_RETURNS, serializable.size());
								} else {
									exchange.response.putInteger(KEYWORD_RETURNS, -1);
								}

								xhead = exchange.response.toJSONStringCompact().getBytes(config.charset);

								ctx.channel().eventLoop().execute(() -> {
									final HttpResponse response;

									response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);

									response.headers()
										  .set(HttpHeaderNames.CONTENT_TYPE, GWXUtilities.formatMimeType(GWXSupportedMimeType.NDJSON, config.charset))
										  .set(HttpHeaderNames.TRANSFER_ENCODING, HttpHeaderValues.CHUNKED);

									ctx.write(response);
									ctx.write(new DefaultHttpContent(Unpooled.copiedBuffer(xhead)));
									ctx.write(preparedChunk());
									ctx.flush();

									Core.executeAsync(() -> {
										serializable.walk((element) -> {
											if (!requireCheckExplicitAccess || session.checkAccess(element, GWXPermission.READ)) {
												final byte[] datax;

												datax = element.serialize(false);

												ctx.channel().eventLoop().execute(() -> {
													ctx.write(new DefaultHttpContent(Unpooled.copiedBuffer(datax)));
													ctx.write(preparedChunk());
													ctx.flush();
												});
											}
										}, () -> {
											ctx.channel().eventLoop().execute(() -> {
												ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT).addListener(ChannelFutureListener.CLOSE);
											});
										});
									});
								});
							} else {
								ctx.channel().eventLoop().execute(() -> {
									final HttpResponse response;

									response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND);

									ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
								});
							}

							whenEnd = System.currentTimeMillis();

							System.out.println("@HttpApiHandler took %d millisecond(s)".formatted(whenEnd - whenBegin));
						});
					});
				}
			}
		});
		}catch(Throwable t){
			t.printStackTrace();
		}
	}
}