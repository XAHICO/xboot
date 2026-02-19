/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.publish;

import com.xahico.boot.pilot.Core;
import com.xahico.boot.reflection.ClassFactory;
import java.io.IOException;
import com.xahico.boot.pilot.ServiceFactorizer;
import com.xahico.boot.pilot.ServiceInitializer;
import com.xahico.boot.pilot.ServiceProvider;
import com.xahico.boot.publish.handlers.*;
import com.xahico.boot.util.Exceptions;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.stream.ChunkedWriteHandler;
import java.io.File;
import java.nio.charset.Charset;
import javax.net.ssl.KeyManagerFactory;
import org.python.util.PythonInterpreter;

/**
 * TBD.
 * 
 * @param <T> 
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class GWXServiceProvider <T extends GWXSession> extends ServiceProvider {
	private static final int DEFAULT_SESSION_TIMEOUT_SECONDS = 10;
	private static final int PORT_INSECURE = 80;
	private static final int PORT_SECURE = 443;
	
	
	
	@ServiceFactorizer
	protected static GWXServiceProvider createService (final GWXService service, final ClassFactory<? extends GWXSession> instanceFactory){
		try {
			return new GWXServiceProvider(instanceFactory);
		} catch (final IOException ex) {
			throw new Error(ex);
		}
	}
	
	@ServiceInitializer
	protected static void initializeService (final GWXService service, final GWXServiceProvider serviceProvider) throws Throwable {
		serviceProvider.setBindPort(service.port());
		serviceProvider.setSessionTimeout(service.timeout());
	}
	
	
	
	private ServerBootstrap               boot = null;
	private ServerBootstrap               bootSSL = null;
	private EventLoopGroup                boss = null;
	private final GWXServiceConfiguration config = new GWXServiceConfiguration();
	private final GWXServiceContext       context;
	private Channel                       listener = null;
	private Channel                       listenerSSL = null;
	private EventLoopGroup                worker = null;
	
	
	
	public GWXServiceProvider (final Class<T> instanceClass) throws IOException {
		this(ClassFactory.getClassFactory(instanceClass));
	}
	
	public GWXServiceProvider (final ClassFactory<T> instanceFactory) throws IOException {
		super();
		
		this.context = new GWXServiceContext(this, (ClassFactory<GWXSession>)instanceFactory);
	}
	
	
	
	@Override
	protected void cleanup (){
		if (null != listener) try {
			listener.closeFuture().sync();
		} catch (final InterruptedException ex) {
			Exceptions.ignore(ex);
		} finally {
			listener = null;
		}
		
		if (null != listenerSSL) try {
			listenerSSL.closeFuture().sync();
		} catch (final InterruptedException ex) {
			Exceptions.ignore(ex);
		} finally {
			listenerSSL = null;
		}
		
		if (null != worker) {
			worker.shutdownGracefully();
			worker = null;
		}
		
		if (null != boss) {
			boss.shutdownGracefully();
			boss = null;
		}
		
		if (null != context.interfaceManager) {
			context.interfaceManager.destroy();
			context.interfaceManager = null;
		}
	}
	
	@Override
	protected void initialize () throws Throwable {
		final SslContext        ctxSSL;
		final KeyManagerFactory kmfSSL;
		
		// preload the Python environment in the background 
		// to prevent initialization overhead on first load --
		// any subsequent loading of the Python env will be very fast
		Core.executeBlocking(() -> {
			final long whenBegin;
			final long whenEnd;
			
			whenBegin = System.currentTimeMillis();
			
			try (final var interpreter = new PythonInterpreter()) {
				interpreter.exec("");
			}
			
			whenEnd = System.currentTimeMillis();
			
			System.out.println("Loaded the Python environment in %g second(s)".formatted(((double)(whenEnd - whenBegin)) / 1000.0));
		});
		
		context.executor = this.getExecutor();
		
		context.rcm = new GWXResourceManager(this, context.instanceFactory.getProductionClass());
		
		if (null != config.interfaceRoot) {
			context.interfaceManager = new GWXAPIInterfaceManager(context.rcm, config.interfaceRoot, this.getExecutor());
		}
		
		if (null != config.webInterfaceFile) {
			context.webNamespace = new GWXNamespace();
			context.webInterface = new GWXWebInterface(config.webInterfaceFile, () -> {
				context.webNamespace.clear();
				
				for (final var variable : context.webInterface.getVariables()) {
					context.webNamespace.set(variable.key, variable.value);
				}
			});
		}
		
		kmfSSL = config.ssl;
		
		if (null != kmfSSL) {
			context.useSSL = true;
		}
		
//		SelfSignedCertificate ssc = new SelfSignedCertificate();
//		ctxSSL = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();

		if (context.useSSL) 
			ctxSSL = SslContextBuilder.forServer(kmfSSL).build();
		else {
			ctxSSL = null;
		}
		
		boss = new NioEventLoopGroup(1);
		worker = new NioEventLoopGroup();
		
		boot = new ServerBootstrap();
		boot.group(boss, worker);
		boot.channel(NioServerSocketChannel.class);
		boot.childHandler(new ChannelInitializer<SocketChannel>() {
			@Override
			protected void initChannel (final SocketChannel channel){
				final ChannelPipeline pipeline;

				pipeline = channel.pipeline();

				pipeline.addLast("gzip", new GWXHttpContentCompressor());
				pipeline.addLast("chunked", new ChunkedWriteHandler());
				pipeline.addLast("httpCodec", new HttpServerCodec());
				pipeline.addLast("aggregator", new HttpObjectAggregator(65536));
				
				if (context.useSSL) {
					if (config.port == 0) {
						// force websocket upgrade handler (not accepting insecure if https is available)
						pipeline.addLast(new GWXWebSocketForceInsecureUpgradeHandler(context, config));
						
						// http-to-https redirect handler
						pipeline.addLast(new GWXHttpInsecureRedirectHandler(context, config));
					} else {
						// ssl handler
						pipeline.addLast("ssl", ctxSSL.newHandler(channel.alloc()));
					}
				}

				if (config.enableEvents) {
					// event root handler
					pipeline.addLast(new WebSocketServerProtocolHandler(GWXHttpApiHandler.PATH_BASE_API));
				}

				// authentication (HTTP only, before WebSocket)
				pipeline.addLast(new GWXAuthHandler(context, config));

				if (config.enableEvents) {
					// websock handshake handler
					pipeline.addLast(new GWXWebSocketUpgradeHandler(context, config));

					// websock keepalive handler
					pipeline.addLast(new GWXWebSocketKeepAliveHandler(context, config));

					if (null != config.webRoot) {
						// sse event handler
						pipeline.addLast(new GWXSseEventHandler(context, config));
					}
				}

				if (config.enableActions) {
					// http api requests
					pipeline.addLast(new GWXHttpApiHandler(context, config));
				}

				if (null != context.webInterface) {
					// http downloads
					pipeline.addLast(new GWXHttpDownloadHandler(context, config));
				}

				if (null != config.webRoot) {
					// http web
					pipeline.addLast(new GWXHttpWebHandler(context, config));
				}

				// lifecycle (cleanup)
				pipeline.addLast(new GWXSessionLifecycleHandler(context, config));
			}
		});
		
		/*
		if (!context.useSSL || (config.port == 0)) {
			boot = new ServerBootstrap();
			boot.group(boss, worker);
			boot.channel(NioServerSocketChannel.class);
			boot.childHandler(new ChannelInitializer<SocketChannel>() {
				@Override
				protected void initChannel (final SocketChannel channel){
					final ChannelPipeline pipeline;

					pipeline = channel.pipeline();
					
					pipeline.addLast("gzip", new GWXHttpContentCompressor());
					pipeline.addLast("chunked", new ChunkedWriteHandler());
					pipeline.addLast("httpCodec", new HttpServerCodec());
					pipeline.addLast("aggregator", new HttpObjectAggregator(65536));

					if (context.useSSL && (config.port == 0)) {
						pipeline.addLast(new GWXWebSocketForceInsecureUpgradeHandler(context, config));
						pipeline.addLast(new GWXHttpInsecureRedirectHandler(context, config));

						return;
					}

					if (config.enableEvents) {
						pipeline.addLast(new WebSocketServerProtocolHandler(GWXHttpApiHandler.PATH_BASE_API));
					}

					// authentication (HTTP only, before WebSocket)
					pipeline.addLast(new GWXAuthHandler(context, config));

					if (config.enableEvents) {
						// websock handshake handler
						pipeline.addLast(new GWXWebSocketUpgradeHandler(context, config));
						
						// websock keepalive handler
						pipeline.addLast(new GWXWebSocketKeepAliveHandler(context, config));
						
						if (null != config.webRoot) {
							// sse event handler
							pipeline.addLast(new GWXSseEventHandler(context, config));
						}
					}
					
					if (config.enableActions) {
						// http api requests
						pipeline.addLast(new GWXHttpApiHandler(context, config));
					}
					
					if (null != context.webInterface) {
						// http downloads
						pipeline.addLast(new GWXHttpDownloadHandler(context, config));
					}
					
					if (null != config.webRoot) {
						// http web
						pipeline.addLast(new GWXHttpWebHandler(context, config));
					}
					
					// lifecycle (cleanup)
					pipeline.addLast(new GWXSessionLifecycleHandler(context, config));
				}
			});
		}
		
		if (context.useSSL) {
			bootSSL = new ServerBootstrap();
			bootSSL.group(boss, worker);
			bootSSL.channel(NioServerSocketChannel.class);
			bootSSL.childHandler(new ChannelInitializer<SocketChannel>() {
				@Override
				protected void initChannel (final SocketChannel channel){
					final ChannelPipeline pipeline;
					
					pipeline = channel.pipeline();
					
					pipeline.addLast("ssl", ctxSSL.newHandler(channel.alloc()));
					pipeline.addLast("gzip", new GWXHttpContentCompressor());
					pipeline.addLast("chunked", new ChunkedWriteHandler());
					pipeline.addLast("httpCodec", new HttpServerCodec());
					pipeline.addLast("aggregator", new HttpObjectAggregator(65536));
					
					if (config.enableEvents) {
						pipeline.addLast(new WebSocketServerProtocolHandler(GWXHttpApiHandler.PATH_BASE_API));
					}
					
					// authentication (HTTP only, before WebSocket)
					pipeline.addLast(new GWXAuthHandler(context, config));
					
					if (config.enableEvents) {
						// websock handshake handler
						pipeline.addLast(new GWXWebSocketUpgradeHandler(context, config));
						
						// websock keepalive handler
						pipeline.addLast(new GWXWebSocketKeepAliveHandler(context, config));
						
						if (null != config.webRoot) {
							// sse event handler
							pipeline.addLast(new GWXSseEventHandler(context, config));
						}
					}
					
					if (config.enableActions) {
						// http api requests
						pipeline.addLast(new GWXHttpApiHandler(context, config));
					}
					
					if (null != context.webInterface) {
						// http downloads
						pipeline.addLast(new GWXHttpDownloadHandler(context, config));
					}
					
					if (null != config.webRoot) {
						// http web
						pipeline.addLast(new GWXHttpWebHandler(context, config));
					}
					
					// lifecycle (cleanup)
					pipeline.addLast(new GWXSessionLifecycleHandler(context, config));
				}
			});
		}
		*/
	}
	
	@Override
	public boolean isIdle (){
		return false;
	}
	
	@Override
	public boolean isStepper (){
		return false;
	}
	
	@Override
	protected void run (){
		try {
			if (null != boot) {
				final int bindPort;
				
				if (config.port != 0) {
					bindPort = config.port;
				} else {
					bindPort = PORT_INSECURE;
				}
				
				listener = boot.bind(bindPort).sync().channel();
				
				System.out.println("Serving raw on port " + bindPort);
			}
			
			if (null != bootSSL) {
				final int bindPort;
				
				if (config.port != 0) {
					bindPort = config.port;
				} else {
					bindPort = PORT_SECURE;
				}
				
				listenerSSL = bootSSL.bind(bindPort).sync().channel();
				
				System.out.println("Serving SSL on port " + bindPort);
			}
		} catch (final InterruptedException ex) {
			Exceptions.ignore(ex);
		}
	}
	
	public void setBindPort (final int port){
		this.config.port = port;
	}
	
	public void setCacheImages (final int minutes){
		this.config.cacheImages = minutes;
	}
	
	public void setCharset (final Charset charset){
		this.config.charset = charset;
	}
	
	public void setEnableActions (final boolean enableActions){
		this.config.enableActions = enableActions;
	}
	
	public void setEnableEvents (final boolean enableEvents){
		this.config.enableEvents = enableEvents;
	}
	
	public void setEventGroupingDispatch (final int seconds){
		this.config.eventGroupingDispatch = seconds;
	}
	
	public void setInterfaceRoot (final File interfaceRoot){
		this.config.interfaceRoot = interfaceRoot;
	}
	
	public void setRetainEvents (final boolean retainEvents){
		this.config.retainEvents = retainEvents;
	}
	
	public void setRetainHandles (final boolean retainHandles){
		this.config.retainHandles = retainHandles;
	}
	
	public void setSessionTimeout (final int timeoutSeconds){
		this.context.sessionTimeout = (timeoutSeconds != 0 ? timeoutSeconds : DEFAULT_SESSION_TIMEOUT_SECONDS);
	}
	
	public void setSSL (final KeyManagerFactory ssl){
		this.config.ssl = ssl;
	}
	
	public void setUseCachedRendering (final boolean useCachedRendering){
		this.config.useCachedRendering = useCachedRendering;
	}
	
	public void setWebClassRoot (final File webClassRoot){
		this.config.webClassRoot = webClassRoot;
	}
	
	public void setWebInterface (final File webInterface){
		this.config.webInterfaceFile = webInterface;
	}
	
	public void setWebRoot (final File webRoot){
		this.config.webRoot = webRoot;
	}
}