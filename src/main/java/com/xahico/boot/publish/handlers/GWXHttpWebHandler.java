/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.publish.handlers;

import com.xahico.boot.io.Source;
import com.xahico.boot.lang.html.HTMLDocument;
import com.xahico.boot.lang.html.HTMLException;
import com.xahico.boot.lang.html.HTMLNode;
import com.xahico.boot.lang.html.HTMLParser;
import com.xahico.boot.lang.html.HTMLStandardType;
import com.xahico.boot.lang.html.HTMLUtilities;
import com.xahico.boot.lang.html.fx.HTFXParser;
import com.xahico.boot.pilot.Core;
import com.xahico.boot.publish.GWXAPIInterface;
import com.xahico.boot.publish.GWXContext;
import com.xahico.boot.publish.GWXImporter;
import com.xahico.boot.publish.GWXInstance;
import com.xahico.boot.publish.GWXInvalidRouteException;
import com.xahico.boot.publish.GWXRoute;
import com.xahico.boot.publish.GWXServiceConfiguration;
import com.xahico.boot.publish.GWXServiceContext;
import com.xahico.boot.publish.GWXSession;
import com.xahico.boot.publish.GWXSupportedMimeType;
import com.xahico.boot.publish.GWXUtilities;
import com.xahico.boot.publish.GWXWebBridgeBuilder;
import com.xahico.boot.publish.GWXWebInterface;
import com.xahico.boot.util.FileCache;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelProgressiveFuture;
import io.netty.channel.ChannelProgressiveFutureListener;
import io.netty.channel.DefaultFileRegion;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class GWXHttpWebHandler extends GWXHandler<FullHttpRequest> {
	private final Map<String, ?> renderingCache;
	private final FileCache      renderingStaticCache;
	
	
	
	public GWXHttpWebHandler (final GWXServiceContext callContext, final GWXServiceConfiguration config){
		super(callContext, config);
		
		if (config.useCachedRendering) {
			this.renderingCache = new ConcurrentHashMap<>();
			this.renderingStaticCache = FileCache.createConcurrentTextFileCache();
		} else {
			this.renderingCache = null;
			this.renderingStaticCache = null;
		}
		
		context.webNamespace.set(GWXWebBridgeBuilder.REF_SELF, GWXWebBridgeBuilder.VAR_SELF);
	}
	
	
	
	@Override
	protected void channelRead0 (final ChannelHandlerContext ctx, final FullHttpRequest request){
		try{
		final GWXRoute   route;
		final GWXSession session;
		final long       whenBegin;

		whenBegin = System.currentTimeMillis();

		try {
			route = GWXRoute.parseMiniString(URLDecoder.decode(request.uri(), config.charset), "/", context.interfaceManager.detectLatestVersion());
		} catch (final GWXInvalidRouteException ex) {
			ex.printStackTrace();

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
			final String               host;
			final Path                 target;
			final long                 targetSize;
			final GWXSupportedMimeType targetType;
			
			if (null != context.webInterface) {
				final GWXContext                  callContext;
				final GWXWebInterface.PathHandler handler;
				final GWXInstance                 instance;
				final String                      result;

				context.webInterface.update();

				if (! context.webInterface.available()) {
					ctx.channel().eventLoop().execute(() -> {
						final HttpResponse response;

						response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_GATEWAY);

						ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
					});
					
					return;
				}

				handler = context.webInterface.lookupPathHandler(route.path, session.isAuthenticated());

				if (null == handler) {
					ctx.channel().eventLoop().execute(() -> {
						final HttpResponse response;

						if (! context.webInterface.existsPathHandler(route.path)) {
							response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_GATEWAY);
						} else {
							response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.FOUND);
							response.headers().set(HttpHeaderNames.LOCATION, "/");
						}

						completeResponse(request, response);

						ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
					});

					return;
				}

				instance = new GWXInstance(context.rcm, 0, session);

				callContext = context.rcm.buildContext(session, handler.pattern, route.path);

				result = handler.call(callContext, instance);

				if (null != result) {
					target = Paths.get(config.webRoot.getAbsolutePath(), result);
				} else {
					target = null;
				}
			} else {
				target = config.webRoot.toPath().resolve(route.path.toString()).normalize();
			}

			if (null == target) {
				ctx.channel().eventLoop().execute(() -> {
					final HttpResponse response;

					response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND);

					completeResponse(request, response);

					ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
				});
				
				return;
			}

			try {
				targetType = GWXUtilities.detectMimeType(target, GWXSupportedMimeType.TEXT);
			} catch (final FileNotFoundException | NoSuchFileException ex) {
				ctx.channel().eventLoop().execute(() -> {
					final HttpResponse response;

					ex.printStackTrace();
					response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND);

					completeResponse(request, response);

					ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
				});

				return;
			} catch (final IOException ex) {
				ex.printStackTrace();
				
				ctx.channel().eventLoop().execute(() -> {
					final HttpResponse response;

					response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.SERVICE_UNAVAILABLE);

					completeResponse(request, response);

					ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
				});

				return;
			}

			host = request.headers().get(HttpHeaderNames.HOST);

			//System.out.println("Serving '%s' (%s)".formatted(target.toAbsolutePath(), targetType));

			if (targetType == GWXSupportedMimeType.HTML) {
				Core.executeBlocking(() -> {
					final String       sdata;
					final byte[]       xdata;

					try {
						sdata = this.loadDocument(session, target.toFile(), context.interfaceManager.requireInterface(route.version), host);
					} catch (final FileNotFoundException | NoSuchFileException ex) {
						ex.printStackTrace();
						
						ctx.channel().eventLoop().execute(() -> {
							final HttpResponse response;

							response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND);

							completeResponse(request, response);

							ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
						});

						return;
					} catch (final IOException ex) {
						ex.printStackTrace();

						ctx.channel().eventLoop().execute(() -> {
							final HttpResponse response;
							
							response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.SERVICE_UNAVAILABLE);

							completeResponse(request, response);

							ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
						});

						return;
					}

					xdata = sdata.getBytes(config.charset);
					
					ctx.channel().eventLoop().execute(() -> {
						final HttpResponse response;
						
						response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);

						completeResponse(request, response);

						response.headers()
							  .set(HttpHeaderNames.CACHE_CONTROL, "no-cache");

						HttpUtil.setContentLength(response, xdata.length);

						response.headers()
							  .set(HttpHeaderNames.CONTENT_TYPE, GWXUtilities.formatMimeType(GWXSupportedMimeType.HTML, config.charset));

						ctx.write(response);
						ctx.write(new DefaultHttpContent(Unpooled.copiedBuffer(xdata)));
						ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT).addListener((final ChannelFuture future) -> {
							long whenEnd = System.currentTimeMillis();

							System.out.println("@HttpWebHandler Serving '%s' (%s) took %d millisecond(s)".formatted(target.toAbsolutePath(), targetType, whenEnd - whenBegin));

							future.channel().close(); // close after completion
						});
					});
				});
			} else if ((targetType == GWXSupportedMimeType.CSS) || (targetType == GWXSupportedMimeType.JAVASCRIPT)) {
				Core.executeBlocking(() -> {
					final String sdata;
					final byte[] xdata;

					try {
						sdata = this.loadResource(session, target.toFile());
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

					xdata = sdata.getBytes(config.charset);

					ctx.channel().eventLoop().execute(() -> {
						final HttpResponse response;

						response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);

						HttpUtil.setContentLength(response, xdata.length);

						completeResponse(request, response);

						response.headers()
							  .set(HttpHeaderNames.CONTENT_TYPE, GWXUtilities.formatMimeType(targetType, config.charset));

						ctx.write(response);
						ctx.write(new DefaultHttpContent(Unpooled.copiedBuffer(xdata)));
						ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT).addListener((final ChannelFuture future) -> {
							long whenEnd = System.currentTimeMillis();

							System.out.println("@HttpWebHandler Serving '%s' (%s) took %d millisecond(s)".formatted(target.toAbsolutePath(), targetType, whenEnd - whenBegin));

							future.channel().close(); // close after completion
						});
					});
				});
			} else {
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
				
				ctx.channel().eventLoop().execute(() -> {
					final HttpResponse  response;
					final ChannelFuture sendFileFuture;

					response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);

					HttpUtil.setContentLength(response, targetSize);

					completeResponse(request, response);

					response.headers().set(HttpHeaderNames.CONTENT_TYPE, GWXUtilities.formatMimeType(targetType, config.charset));

					if (targetType.isImage() && (config.cacheImages > 0)) {
						response.headers().set(HttpHeaderNames.CACHE_CONTROL, "public, max-age=" + config.cacheImages);
					}

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

							System.out.println("@HttpWebHandler Serving '%s' (%s) took %d millisecond(s)".formatted(target.toAbsolutePath(), targetType, whenEnd - whenBegin));
						}
					});

					// Write last content to mark end of response
					ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
				});
			}
		});
		}catch(Throwable t){
			t.printStackTrace();
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

	private String createClientSDK (final GWXSession session, final GWXAPIInterface iface, final String host){
		final GWXWebBridgeBuilder builder;
		final String              callURL;
		final StringBuilder       callURLBuilder;

		callURLBuilder = new StringBuilder();
		callURLBuilder.append(GWXHttpApiHandler.PATH_BASE_API);
		callURLBuilder.append("/");
		callURLBuilder.append("v");
		callURLBuilder.append((int)iface.version);

		// resolves to e.g. 'http://localhost:80/api/v1/'
		callURL = callURLBuilder.toString();

		builder = new GWXWebBridgeBuilder(session, iface, callURL);
		builder.setSmartConnectionsEnabled(config.retainEvents);

		return builder.build();
	}

	private String loadDocument (final GWXSession session, final File file, final GWXAPIInterface iface, final String host) throws IOException {
		final String       data;
		final HTMLDocument document;
		HTMLNode           documentHead;
		final HTMLNode     documentRoot;

		if (null != config.webClassRoot) {
			final HTFXParser documentLoader;

			documentLoader = new HTFXParser();
			documentLoader.setClassDirectory(config.webClassRoot.getPath());

			if (config.useCachedRendering) {
				documentLoader.setCache(renderingCache);
			}

			if (null != renderingStaticCache) {
//				documentLoader.setSource(Source.wrapString(renderingStaticCache.load(file)));
				documentLoader.setSource(Source.wrapFile(file));
			} else {
				documentLoader.setSource(Source.wrapFile(file));
			}

			try {
				document = documentLoader.parse();
			} catch (final HTMLException ex) {
				throw new Error(ex);
			}
		} else {
			try {
				final String datax;

				if (null != renderingStaticCache) {
					datax = renderingStaticCache.load(file);
				} else {
					datax = Files.readString(file.toPath());
				}

				document = HTMLParser.parseString(datax);
			} catch (final HTMLException ex) {
				throw new Error(ex);
			}

			document.removeSpecialElements();
			document.removeComments();
		}

		documentRoot = document.lookupFirst("html");

		documentHead = document.lookupFirst(HTMLStandardType.HEAD, -1);

		if ((null == documentHead) && (null != documentRoot)) {
			documentHead = new HTMLNode(HTMLStandardType.HEAD);
			documentRoot.getChildren().add(documentHead);
		}

		if ((config.enableActions || config.enableEvents) && (null != documentHead)) {
			documentHead.addChild(HTMLUtilities.createScript(createClientSDK(session, iface, host)));
		}

		document.removeSpecialElements();
		document.removeComments();

		data = document.toHTMLString();
//			data = document.toHTMLStringHumanUnreadable();

		return GWXImporter.importString(data, session.namespace);
	}

	private String loadResource (final GWXSession session, final File file) throws IOException {
		final String datax;

		if (null != renderingStaticCache) {
			datax = renderingStaticCache.load(file);
		} else {
			datax = Files.readString(file.toPath());
		}

		return GWXImporter.importString(datax, session.namespace);
	}
}