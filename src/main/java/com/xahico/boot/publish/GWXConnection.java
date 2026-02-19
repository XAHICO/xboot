/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.publish;

import com.xahico.boot.lang.jsox.JSOXVariant;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.WebSocket;
import java.net.http.WebSocket.Listener;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.GZIPInputStream;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class GWXConnection implements AutoCloseable {
	private static Executor defaultExecutor = null;
	
	
	
	public static GWXConnection create (final String host, final int port, final GWXConnectionConfigurator configurator){
		final GWXConnectionConfiguration config;
		
		config = new GWXConnectionConfiguration();
		
		configurator.configure(config);
		
		return new GWXConnection(host, port, config);
	}
	
	private static Executor getOrCreateDefaultExecutor (){
		if (null != defaultExecutor) {
			return defaultExecutor;
		} else {
			return Executors.newSingleThreadExecutor();
		}
	}
	
	public static void setDefaultExecutor (final Executor executor){
		defaultExecutor = executor;
	}
	
	
	
	private final GWXConnectionConfiguration config;
	private final Set<EventListener>         eventListeners = new CopyOnWriteArraySet<>();
	private final Executor                   executor;
	private final String                     host;
	private HttpClient                       http = null;
	private final int                        port;
	private final AtomicReference<String>    token = new AtomicReference<>(null);
	
	
	
	private GWXConnection (final String host, final int port, final GWXConnectionConfiguration config){
		super();
	  
		this.host = host;
		this.port = port;
		this.config = config;
		this.executor = ((null != config.getExecutor()) ? config.getExecutor() : getOrCreateDefaultExecutor());
		this.http = HttpClient.newBuilder().executor(this.executor).version(HttpClient.Version.HTTP_1_1).build();
	}
	
	
	
	@Override
	public void close (){
		for (final var eventListener : this.eventListeners) {
			eventListener.close();
		}
		
		this.eventListeners.clear();
	}
	
	public int getConnectPort (){
		if (this.port != 0) {
			return this.port;
		} else if (config.getUseTLS()) {
			return 443;
		} else {
			return 80;
		}
	}
	
	public String getHostname (){
		final StringBuilder sb;
		
		sb = new StringBuilder();
		sb.append(this.host);
		sb.append(":");
		sb.append(this.getConnectPort());
		
		return sb.toString();
	}
	
	public String getInputBaseURL (){
		final StringBuilder sb;
		
		sb = new StringBuilder();
		
		if (config.getUseTLS()) {
			sb.append("https");
		} else {
			sb.append("http");
		}
		
		sb.append("://");
		sb.append(this.getHostname());
		sb.append("/api/v");
		sb.append(config.getRequestVersion());
		sb.append("/");
		
		return sb.toString();
	}
	
	public String getOutputBaseURL (){
		final StringBuilder sb;
		
		sb = new StringBuilder();
		
		if (config.getUseTLS()) {
			sb.append("wss");
		} else {
			sb.append("ws");
		}
		
		sb.append("://");
		sb.append(this.getHostname());
		sb.append("/api/v");
		sb.append(config.getRequestVersion());
		sb.append("/");
		
		return sb.toString();
	}
	
	public String getToken (){
		return this.token.get();
	}
	
	public EventChannel listen (final String eventId, final GWXEventObjectHandler handler){
		final WebSocket.Builder builder;
		final EventListener     listener;
		final String            url;
		final String            useToken;
		
		useToken = this.getToken();
		
		if (null == useToken) {
			return null;
		}
		
		url = (this.getOutputBaseURL() + eventId);
		
		listener = new EventListener(handler);
		
		builder = http.newWebSocketBuilder();
		builder.header("Authorization", ("Bearer " + useToken));
		builder.buildAsync(URI.create(url), listener);
		
		return new EventChannel(listener);
	}
	
	public void setToken (final String token){
		this.token.set(token);
	}
	
	public void transactAsync (final String method, final String path, final GWXTransactionHandler handler){
		final JSOXVariant               datax;
		final CompletableFuture<Void>   future;
		final HttpRequest               request;
		final HttpRequest.BodyPublisher requestBody;
		final HttpRequest.Builder       requestBuilder;
		final String                    url;
		final StringBuilder             urlBuilder;
		final String                    useToken;
		
		useToken = this.getToken();
		
		urlBuilder = new StringBuilder();
		urlBuilder.append(this.getInputBaseURL());
		urlBuilder.append(path);
		
		datax = new JSOXVariant();
		
		handler.onRequest(datax);
		
		if (method.equalsIgnoreCase("DELETE") || method.equalsIgnoreCase("GET")) {
			requestBody = HttpRequest.BodyPublishers.noBody();
			
			for (final var key : datax.keySet()) {
				final String encodedKey;
				final String encodedValue;
				
				if (urlBuilder.length() > 0) {
					urlBuilder.append("&");
				}
				
				encodedKey = URLEncoder.encode(key, StandardCharsets.UTF_8);
				encodedValue = URLEncoder.encode(datax.get(key).toString(), StandardCharsets.UTF_8);
				
				urlBuilder.append(encodedKey).append("=").append(encodedValue);
			}
		} else {
			requestBody = HttpRequest.BodyPublishers.ofString(datax.toString());
		}
		
		url = urlBuilder.toString();
		System.out.println("Querying URL: " + url);
		
		requestBuilder = HttpRequest.newBuilder();
		requestBuilder.uri(URI.create(url));
		requestBuilder.method(method, requestBody);
		requestBuilder.header("Content-Type", "application/json; charset=utf-8");
		requestBuilder.header("Accept", "application/x-ndjson");
		requestBuilder.header("Accept-Encoding", "gzip");
		
		if (null != useToken) {
			requestBuilder.header("Authorization", ("Bearer " + useToken));
		}
		
		request = requestBuilder.build();
		
		future = http.sendAsync(request, HttpResponse.BodyHandlers.ofInputStream()).thenAcceptAsync(response -> {
			try (final var raw = response.body()) {
				InputStream    in = raw;
				String         line;
				BufferedReader reader;
				boolean        responded = false;
				JSOXVariant    responsex = null;
				boolean        streamed = false;
				boolean        status = true;
				
				if ("gzip".equalsIgnoreCase(response.headers().firstValue("Content-Encoding").orElse(""))) {
					in = new GZIPInputStream(raw);
				}
				
				reader = new BufferedReader(new InputStreamReader(in));
				
				while ((line = reader.readLine()) != null) {
					final JSOXVariant variant;
					
					if (line.isBlank()) {
						break;
					}
					
					variant = new JSOXVariant(line);
					
					if (null == responsex) {
						responsex = variant;
						
						if (variant.has("error")) {
							status = false;
						}
						
						continue;
					}
					
					if (! streamed) {
						streamed = true;
						
						if (! responded) {
							responded = true;
							
							handler.onResponse(responsex, true);
						}
						
						handler.onStreamOpen();
					}
					
					handler.onStream(variant);
				}
				
				if (streamed) {
					handler.onStreamClose();
				} else {
					handler.onResponse(responsex, false);
				}
				
				handler.onFinalize(status);
			} catch (final IOException ex) {
				handler.onError(ex);
			} catch (final Throwable t) {
				handler.onError(t);
			}
            }).exceptionallyAsync((error) -> {
			handler.onError(error);
			
			return null;
		});
		
//		future.join();
	}
	
	
	
	public final class EventChannel {
		private final EventListener listener;
		
		
		
		private EventChannel (final EventListener listener){
			super();
			
			this.listener = listener;
			
			GWXConnection.this.eventListeners.add(listener);
		}
		
		
		
		public void close (){
			this.listener.close();
			
			GWXConnection.this.eventListeners.remove(this.listener);
		}
	}
	
	private final class EventListener implements Listener {
		private final GWXEventObjectHandler handler;
		private WebSocket                   socket = null;
		
		
		
		private EventListener (final GWXEventObjectHandler handler){
			super();
			
			this.handler = handler;
		}
		
		
		
		public void close (){
			if (null != this.socket) {
				this.socket.sendClose(WebSocket.NORMAL_CLOSURE, "Closing");
			}
		}
		
		@Override
		public CompletionStage<?> onClose (final WebSocket webSocket, final int statusCode, final String reason){
			this.socket = null;
			System.out.println("WebSocket closed: " + statusCode + " " + reason);
			return Listener.super.onClose(webSocket, statusCode, reason);
		}
		
		@Override
		public void onError (final WebSocket webSocket, final Throwable error){
			System.err.println("WebSocket error: " + error.getMessage());
		}
		
		@Override
		public void onOpen (final WebSocket webSocket){
			this.socket = webSocket;
			System.out.println("WebSocket opened!");
			Listener.super.onOpen(webSocket);
		}
		
		@Override
		public CompletionStage<?> onText (final WebSocket webSocket, final CharSequence data, final boolean last){
			final GWXEventObject event;
			
			event = new GWXEventObject();
			event.assume(data.toString());
			
			this.handler.handle((event.target + "/" + event.id), event);
			
			return Listener.super.onText(webSocket, data, last);
		}
		
		public boolean ping (){
			if (null == this.socket) {
				return false;
			} else {
				this.socket.sendPing(ByteBuffer.wrap(new byte[0]));
				
				return true;
			}
		}
	}
}