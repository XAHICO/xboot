/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.net.web.http;

import com.sun.net.httpserver.HttpServer;
import com.xahico.boot.pilot.InitializeException;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsExchange;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;
import com.xahico.boot.logging.Logger;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public class HttpServiceProvider extends HttpServiceProviderBase {
	private static final String QUERY_ACTION_ID = "id";
	private static final String QUERY_EVENT_ID = "id";
	
	private static final String KEY_UNSCOPED = "*";
	private static final String PATH_ACTION = "/action";
	private static final String PATH_EVENT = "/event";
	
	
	
	private final Map<String, HttpServiceHandler> actionHandlers = new HashMap<>();
	private String                                domain = null;
	private final HttpServiceEnvironment          environment = new HttpServiceEnvironment();
	private final Map<String, HttpServiceHandler> eventHandlers = new HashMap<>();
	private HttpServiceHandler                    initializeHandler = null;
	private HttpServiceHandler                    routingHandler = null;
	private HttpServer                            server = null;
	private boolean                               ssl = false;
	private SSLContext                            sslContext = null;
	
	
	
	public HttpServiceProvider (){
		super();
	}
	
	
	
	private void callInitializeHandler (final HttpServiceEnvironment environment, final HttpServiceClient client, final HttpServiceExchange exchange) throws InitializeException, IOException {
		if (null != this.initializeHandler) {
			try {
				this.initializeHandler.handle(environment, client, exchange);
			} catch (final IOException ex) {
				throw ex;
			} catch (final Exception ex) {
				throw new InitializeException(ex);
			}
		}
	}
	
	@Override
	protected void cleanup (){
		server.stop(0);
	}
	
	public HttpServiceHandler getActionHandler (final String actionId){
		HttpServiceHandler handler;
		
		handler = this.actionHandlers.get(actionId.toLowerCase());
		
		if (null == handler) {
			handler = this.actionHandlers.get(KEY_UNSCOPED);
		}
		
		return handler;
	}
	
	public HttpServiceHandler getEventSubscriptionHandler (final String eventId){
		HttpServiceHandler handler;
		
		handler = this.eventHandlers.get(eventId.toLowerCase());
		
		if (null == handler) {
			handler = this.eventHandlers.get(KEY_UNSCOPED);
		}
		
		return handler;
	}
	
	public final HttpServiceEnvironment getEnvironment (){
		return this.environment;
	}
	
	@Override
	public int getPort (){
		final InetSocketAddress address;
		
		if (null == this.server) 
			return -1;
		
		address = this.server.getAddress();
		
		if (null == address) 
			return -1;
		
		return address.getPort();
	}
	
	public URL getRemoteName (){
		try {
			final URLBuilder builder;
			
			builder = new URLBuilder();
			builder.setHost((null != this.domain) ? this.domain : "[::1]");
			builder.setPort(this.getPort());
			builder.setProtocol((ssl) ? HttpProtocol.HTTPS : HttpProtocol.HTTP);
			
			return builder.build();
		} catch (final MalformedURLException ex) {
			throw new Error(ex);
		}
	}
	
	@Override
	protected void initialize () throws Throwable {
		if (ssl) 
			this.initializeWithSSL();
		else {
			this.initializeWithoutSSL();
		}
	}
	
	@SuppressWarnings("UseSpecificCatch")
	private void initializeWithoutSSL () throws Throwable {
		server = HttpServer.create();
		server.setExecutor(this.getExecutor());
		server.bind(new InetSocketAddress(this.getBindPort()), 0);
		server.createContext("/", (httpExchange) -> {
			final HttpServiceClient   client;
			final HttpServiceExchange exchange;
			final Logger.Entry        logEntry;
			final String              target;
			final long                whenBegin;
			final long                whenEnd;
			
			whenBegin = System.currentTimeMillis();
			
			target = httpExchange.getRequestURI().getPath();
			
			client = new HttpServiceClient(httpExchange);
			
			logEntry = this.getLogger().newEntry();
			logEntry.write("")
			        .write(httpExchange.getRequestMethod())
			        .write(" ")
			        .write("\'")
			        .write(Objects.toString(httpExchange.getRequestURI()))
			        .write("\'")
			        .writeLine();
			
			exchange = new HttpServiceExchange(httpExchange);
			
			try {
				if (target.equalsIgnoreCase(PATH_ACTION)) {
					final HttpServiceHandler  actionHandler;
					final String              actionId;
					
					exchange.getResponseHeaders().add("Cache-Control", "no-cache");

					actionId = exchange.getRequestTarget().getQueryParameter(QUERY_ACTION_ID);

					actionHandler = this.getActionHandler(actionId);

					if (null != actionHandler) {
						callInitializeHandler(environment, client, exchange);

						actionHandler.handle(environment, client, exchange);
					}
				} else if (target.equalsIgnoreCase(PATH_EVENT)) {
					final HttpServiceHandler eventHandler;
					final String             eventId;
					
					exchange.getResponseHeaders().add("Cache-Control", "no-cache");

					eventId = exchange.getRequestTarget().getQueryParameter(QUERY_EVENT_ID);

					eventHandler = this.getEventSubscriptionHandler(eventId);

					if (null != eventHandler) {
						callInitializeHandler(environment, client, exchange);

						eventHandler.handle(environment, client, exchange);
					}
				} else {
					callInitializeHandler(environment, client, exchange);
					
					if (null != this.routingHandler) 
						this.routingHandler.handle(environment, client, exchange);
					else {
						exchange.sendResponseNotFound();
					}
				}
			} catch (final InitializeException ex) {
				logEntry.writeLine(ex);
				
				exchange.sendResponseNotFound();
			} catch (final Throwable t) {
				logEntry.writeLine(t);
			} finally {
				whenEnd = System.currentTimeMillis();
				
				logEntry.write("Handled in %d millisecond(s)".formatted(whenEnd - whenBegin));
				logEntry.close();
			}
		});
	}
	
	@SuppressWarnings("UseSpecificCatch")
	private void initializeWithSSL () throws Throwable {
		final HttpsServer serverSSL;
		
		serverSSL = HttpsServer.create();
		serverSSL.setExecutor(this.getExecutor());
		serverSSL.bind(new InetSocketAddress(this.getBindPort()), 0);
		serverSSL.createContext("/", (httpExchange) -> {
			final HttpServiceClient   client;
			final HttpServiceExchange exchange;
			final HttpsExchange       httpsExchange;
			final Logger.Entry        logEntry;
			final String              target;
			final long                whenBegin;
			final long                whenEnd;
			
			httpsExchange = (HttpsExchange)(httpExchange);
			
			whenBegin = System.currentTimeMillis();
			
			target = httpExchange.getRequestURI().getPath();
			
			client = new HttpServiceClient(httpExchange);
			
			logEntry = this.getLogger().newEntry();
			logEntry.write("")
			        .write(httpExchange.getRequestMethod())
			        .write(" ")
			        .write("\'")
			        .write(Objects.toString(httpExchange.getRequestURI()))
			        .write("\'")
			        .writeLine();
			
			exchange = new HttpServiceExchange(httpExchange);
			
			try {
				if (target.equalsIgnoreCase(PATH_ACTION)) {
					final HttpServiceHandler  actionHandler;
					final String              actionId;
					
					exchange.getResponseHeaders().add("Cache-Control", "no-cache");

					actionId = exchange.getRequestTarget().getQueryParameter(QUERY_ACTION_ID);

					actionHandler = this.getActionHandler(actionId);

					if (null != actionHandler) {
						callInitializeHandler(environment, client, exchange);

						actionHandler.handle(environment, client, exchange);
					}
				} else if (target.equalsIgnoreCase(PATH_EVENT)) {
					final HttpServiceHandler eventHandler;
					final String             eventId;
					
					exchange.getResponseHeaders().add("Cache-Control", "no-cache");

					eventId = exchange.getRequestTarget().getQueryParameter(QUERY_EVENT_ID);

					eventHandler = this.getEventSubscriptionHandler(eventId);

					if (null != eventHandler) {
						callInitializeHandler(environment, client, exchange);

						eventHandler.handle(environment, client, exchange);
					}
				} else {
					callInitializeHandler(environment, client, exchange);
					
					if (null != this.routingHandler) 
						this.routingHandler.handle(environment, client, exchange);
					else {
						exchange.sendResponseNotFound();
					}
				}
			} catch (final InitializeException ex) {
				logEntry.writeLine(ex);
				
				exchange.sendResponseNotFound();
			} catch (final Throwable t) {
				logEntry.writeLine(t);
			} finally {
				whenEnd = System.currentTimeMillis();
				
				logEntry.write("Handled in %d millisecond(s)".formatted(whenEnd - whenBegin));
				logEntry.close();
			}
		});
		serverSSL.setHttpsConfigurator(new HttpsConfigurator(sslContext) {
			@Override
			public void configure (final HttpsParameters params){
				final SSLContext    sslContext;
				final SSLEngine     sslEngine;
				final SSLParameters sslParams;

				sslContext = getSSLContext();

				sslEngine = sslContext.createSSLEngine();
				
				params.setNeedClientAuth(false);
				params.setCipherSuites(sslEngine.getEnabledCipherSuites());
				params.setProtocols(sslEngine.getEnabledProtocols());

				sslParams = sslContext.getSupportedSSLParameters();

				params.setSSLParameters(sslParams);
			}
		});
		
		server = serverSSL;
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
		server.start();
	}
	
	public void setActionHandler (final String actionId, final HttpServiceHandler handler){
		this.actionHandlers.put(actionId.toLowerCase(), handler);
	}
	
	public void setDomain (final String domain){
		this.domain = domain;
	}
	
	public void setEventSubscriptionHandler (final String eventId, final HttpServiceHandler handler){
		this.eventHandlers.put(eventId.toLowerCase(), handler);
	}
	
	public void setInitializeHandler (final HttpServiceHandler handler){
		this.initializeHandler = handler;
	}
	
	public void setRoot (final File root){
		this.environment.root(root);
	}
	
	public void setRoutingHandler (final HttpServiceHandler handler){
		this.routingHandler = handler;
	}
	
	public void setSSL (final boolean ssl){
		this.ssl = ssl;
	}
	
	public void setSSLContext (final SSLContext sslContext){
		this.sslContext = sslContext;
	}
}