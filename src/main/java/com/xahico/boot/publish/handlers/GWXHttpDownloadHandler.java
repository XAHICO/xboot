/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.publish.handlers;

import com.xahico.boot.pilot.Core;
import com.xahico.boot.publish.GWXContext;
import com.xahico.boot.publish.GWXInstance;
import com.xahico.boot.publish.GWXInvalidRouteException;
import com.xahico.boot.publish.GWXRoute;
import com.xahico.boot.publish.GWXServiceConfiguration;
import com.xahico.boot.publish.GWXServiceContext;
import com.xahico.boot.publish.GWXSession;
import com.xahico.boot.publish.GWXSupportedMimeType;
import com.xahico.boot.publish.GWXUtilities;
import com.xahico.boot.publish.GWXWebInterface;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelProgressiveFuture;
import io.netty.channel.ChannelProgressiveFutureListener;
import io.netty.channel.DefaultFileRegion;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class GWXHttpDownloadHandler extends GWXHandler<FullHttpRequest> {
	public static final String PATH_BASE_DOWNLOAD = "/get";
	
	
	
	public GWXHttpDownloadHandler (final GWXServiceContext callContext, final GWXServiceConfiguration config){
		super(callContext, config);
	}
	
	
	
	@Override
	protected void channelRead0 (final ChannelHandlerContext ctx, final FullHttpRequest request){
		final GWXRoute   route;
		final GWXSession session;
		final long       whenBegin;

		whenBegin = System.currentTimeMillis();

		try {
			route = GWXRoute.parseSemiString(URLDecoder.decode(request.uri(), config.charset), "/", -1);
		} catch (final GWXInvalidRouteException ex) {
			ctx.fireChannelRead(request.retain());

			return;
		}

		if (! route.root.equalsIgnoreCase(PATH_BASE_DOWNLOAD)) {
			ctx.fireChannelRead(request.retain());

			return;
		}

		if (! request.method().equals(HttpMethod.GET)) {
			ctx.fireChannelRead(request.retain());

			return;
		}

		session = ctx.channel().attr(context.keySession).get();

		if (null == session) {
			ctx.fireChannelRead(request.retain());

			return;
		}
		
		// jump to blocking executor for interface loading & file I/O (this may block)
		Core.executeBlocking(() -> {
			final Path                 target;
			final long                 targetSize;
			final GWXSupportedMimeType targetType;
			
			if (null != context.webInterface) {
				final GWXContext                      callContext;
				final GWXWebInterface.ArtifactHandler handler;
				final GWXInstance                     instance;
				final String                          result;

				context.webInterface.update();

				if (! context.webInterface.available()) {
					ctx.channel().eventLoop().execute(() -> {
						final HttpResponse response;

						response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_GATEWAY);

						ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
					});

					return;
				}

				handler = context.webInterface.lookupArtifactHandler(route.path, session.isAuthenticated());

				if (null == handler) {
					ctx.channel().eventLoop().execute(() -> {
						final HttpResponse response;

						response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_GATEWAY);

						ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
					});

					return;
				}

				instance = new GWXInstance(context.rcm, 0, session);

				callContext = context.rcm.buildContext(session, handler.pattern, route.path);

				result = handler.call(callContext, instance);

				if (null == result) {
					ctx.channel().eventLoop().execute(() -> {
						final HttpResponse response;

						response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND);

						ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
					});

					return;
				}

				target = Paths.get(result);
			} else {
				target = config.webRoot.toPath().resolve(route.path.toString()).normalize();
			}

			if (null == target) {
				ctx.channel().eventLoop().execute(() -> {
					final HttpResponse response;
					
					response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND);
					
					ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
				});

				return;
			}
			
			try {
				targetSize = Files.size(target);
			} catch (final FileNotFoundException | NoSuchFileException ex) {
				ctx.channel().eventLoop().execute(() -> {
					final HttpResponse response;
					
					response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND);

					ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
				});

				return;
			} catch (final IOException ex) {
				ctx.channel().eventLoop().execute(() -> {
					final HttpResponse response;
					
					response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.SERVICE_UNAVAILABLE);

					ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
				});

				return;
			}

			try {
				targetType = GWXUtilities.detectMimeType(target, GWXSupportedMimeType.BINARY_STREAM);
			} catch (final FileNotFoundException | NoSuchFileException ex) {
				ctx.channel().eventLoop().execute(() -> {
					final HttpResponse response;
					
					response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND);

					ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
				});

				return;
			} catch (final IOException ex) {
				ctx.channel().eventLoop().execute(() -> {
					final HttpResponse response;
					
					response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.SERVICE_UNAVAILABLE);

					ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
				});

				return;
			}

			ctx.channel().eventLoop().execute(() -> {
				final HttpResponse  response;
				final ChannelFuture sendFileFuture;

				response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);

				HttpUtil.setContentLength(response, targetSize);

				response.headers().set(HttpHeaderNames.CONTENT_TYPE, targetType.toString());

				ctx.write(response);

				sendFileFuture = ctx.write(new DefaultFileRegion(target.toFile(), 0, targetSize), ctx.newProgressivePromise());

				sendFileFuture.addListener(new ChannelProgressiveFutureListener() {
					@Override
					public void operationProgressed (final ChannelProgressiveFuture future, final long progress, final long total){
						// optional: progress log
					}

					@Override
					public void operationComplete (final ChannelProgressiveFuture future){
						final long whenEnd;

						whenEnd = System.currentTimeMillis();

						System.out.println("@HttpDownloadHandler took %d millisecond(s)".formatted(whenEnd - whenBegin));
					}
				});

				// Write last content to mark end of response
				ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
			});
		});
	}
}