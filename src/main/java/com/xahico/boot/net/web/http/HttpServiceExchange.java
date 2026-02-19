/*
 * Written by Tuomas Kontiainen <https://www.github.com/tutomiko>
 * 
 * Copyright (c) 2023, Tuomas Kontiainen
 */
package com.xahico.boot.net.web.http;

import com.sun.net.httpserver.HttpExchange;
import com.xahico.boot.lang.json.JSON;
import com.xahico.boot.net.URIA;
import com.xahico.boot.lang.jsox.JSOXObject;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.util.List;
import org.json.JSONObject;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public class HttpServiceExchange {
	private Charset              charset = UTF_8;
	protected final HttpExchange exchange;
	
	
	
	HttpServiceExchange (final HttpExchange exchange){
		super();
		
		this.exchange = exchange;
	}
	
	
	
	public final InputStream getRequestBody (){
		return this.exchange.getRequestBody();
	}
	
	public final String getRequestHeader (final String key){
		final List<String> values;
		
		values = this.getRequestHeaders().get(key);
		
		if (null == values) 
			return null;
		
		return values.get(values.size() - 1);
	}
	
	public final HttpHeaders getRequestHeaders (){
		return HttpHeaders.wrap(this.exchange.getRequestHeaders());
	}
	
	public final HttpMethod getRequestMethod () throws InvalidMethodException {
		final String methodString;
		
		methodString = this.getRequestMethodString();
		
		for (final var method : HttpMethod.values()) {
			if (method.name().equalsIgnoreCase(methodString)) {
				return method;
			}
		}
		
		throw new InvalidMethodException(String.format("Invalid method \'%s\'", methodString));
	}
	
	public final String getRequestMethodString (){
		return exchange.getRequestMethod();
	}
	
	public final URIA getRequestTarget (){
		return URIA.transform(exchange.getRequestURI());
	}
	
	public final String getRequestTargetPath (){
		return this.getRequestTarget().getPath();
	}
	
	public final OutputStream getResponseBody (){
		return this.exchange.getResponseBody();
	}
	
	public final HttpHeaders getResponseHeaders (){
		return HttpHeaders.wrap(this.exchange.getResponseHeaders());
	}
	
	public final String readRequestBodyData () throws IOException {
		final byte[] dataBytes;
		final String dataString;
		
		dataBytes = exchange.getRequestBody().readAllBytes();
		
		dataString = new String(dataBytes, charset);
		
		return dataString;
	}
	
	public final JSONObject readRequestBodyJSON () throws IOException {
		final String data;
		
		data = this.readRequestBodyData();
		
		return JSON.newObject(data);
	}
	
	public final <T extends JSOXObject> T readRequestBodyJSOX (final Class<T> jclass) throws IOException {
		final JSONObject json;
		final T          jsox;
		
		json = this.readRequestBodyJSON();
		
		jsox = JSOXObject.newInstanceOf(jclass, json);
		
		return jsox;
	}
	
	public final void sendEvent (final HttpEvent event) throws IOException {
		exchange.getResponseBody().write(event.toString().getBytes(UTF_8)); // Always UTF-8!
		exchange.getResponseBody().flush();
	}
	
	public final void sendResponse (final HttpStatus status, final int contentLength) throws IOException {
		exchange.sendResponseHeaders(status.code(), contentLength);
	}
	
	public final void sendResponseData (final HttpStatus status, final byte[] data, final HttpMimeType dataType) throws IOException {
		exchange.getResponseHeaders().add("Content-Type", dataType.string());
		exchange.sendResponseHeaders(status.code(), data.length);
		
		try (final var stream = exchange.getResponseBody()) {
			stream.write(data);
			stream.flush();
		}
	}
	
	public final void sendResponseData (final HttpStatus status, final String data, final HttpMimeType dataType) throws IOException {
		final byte[] buffer;
		
		buffer = data.getBytes(charset);
		
		exchange.getResponseHeaders().add("Content-Type", (dataType.string() + "; charset=UTF-8"));
		exchange.sendResponseHeaders(status.code(), buffer.length);
		
		try (final var stream = exchange.getResponseBody()) {
			stream.write(buffer);
			stream.flush();
		}
	}
	
	public final void sendResponseEventHandlingBegins () throws IOException {
		exchange.getResponseHeaders().add("Content-Type", HttpMimeType.SSE.string());
		exchange.getResponseHeaders().add("Connection", "keep-alive");
		exchange.getResponseHeaders().add("Keep-Alive", "100000000"); // -1
		exchange.getResponseHeaders().add("Cache-Control", "no-cache");
		exchange.getResponseHeaders().add("Content-Encoding", "none");
		exchange.sendResponseHeaders(HttpStatus.STATUS_OK.code(), 0);
	}
	
	public final void sendResponseEventHandlingEnds () throws IOException {
		exchange.getResponseBody().close();
	}
	
	public final void sendResponseFile (final File file) throws IOException {
		final HttpMimeType dataType;
		final long         fileSize;
		
		dataType = HttpUtilities.getFileHttpType(file);
		
		fileSize = file.length();
		
		exchange.getResponseHeaders().add("Content-Type", (dataType.string() + "; charset=UTF-8"));
		exchange.sendResponseHeaders(HttpStatus.STATUS_OK.code(), fileSize);

		try (final var in = new FileInputStream(file)) {
			try (final var out = exchange.getResponseBody()) {
				in.transferTo(out);
			}
		}
	}
	
	public final void sendResponseJSON (final HttpStatus status, final JSONObject json) throws IOException {
		this.sendResponseData(status, json.toString(), HttpMimeType.JSON);
	}
	
	public final void sendResponseJSOX (final HttpStatus status, final JSOXObject jsox) throws IOException {
		this.sendResponseJSON(status, jsox.json());
	}
	
	public final void sendResponseNotFound () throws IOException {
		exchange.sendResponseHeaders(HttpStatus.STATUS_NOT_FOUND.code(), -1);
	}
	
	public final void sendResponseRedirect (final URIA uri) throws IOException {
		this.sendResponseRedirect(uri.toString());
	}
	
	public final void sendResponseRedirect (final String url) throws IOException {
		exchange.getResponseHeaders().add("Location", url);
		exchange.sendResponseHeaders(HttpStatus.STATUS_REDIRECT.code(), -1);
	}
	
	public final void sendResponseUnauthorized () throws IOException {
		exchange.sendResponseHeaders(HttpStatus.STATUS_UNAUTHORIZED.code(), -1);
	}
	
	public final void setCharset (final Charset charset){
		this.charset = charset;
	}
}