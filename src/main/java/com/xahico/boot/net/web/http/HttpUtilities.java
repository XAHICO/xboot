/*
 * Written by Tuomas Kontiainen <https://www.github.com/tutomiko>
 * 
 * Copyright (c) 2023, Tuomas Kontiainen
 */
package com.xahico.boot.net.web.http;

import com.xahico.boot.util.ArrayUtilities;
import com.xahico.boot.util.Exceptions;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class HttpUtilities {
	public static HttpMimeType getFileHttpType (final File file){
		return getFileHttpType(file.getName());
	}
	
	public static HttpMimeType getFileHttpType (final String fileName){
		final int    delimiter;
		final String fileExtension;
		
		delimiter = fileName.lastIndexOf('.');
		
		if (delimiter == -1) 
			return HttpMimeType.TEXT;
		
		fileExtension = fileName.substring(delimiter);
		
		for (final var type : HttpMimeType.values()) {
			for (final var acceptableExtension : type.extensions()) {
				if (acceptableExtension.equalsIgnoreCase(fileExtension)) {
					return type;
				}
			}
		}

		return HttpMimeType.TEXT;
	}
	
	public static void transactTunneled (final URL url, final HttpServiceExchange exchange, final boolean closeOutput, final String... ignoreHeaders) throws HttpException, IOException, MalformedURLException {
		try {
			final HttpURLConnection connection;
			final String            errorMessage;
			final HttpMethod        method;
			final byte[]            response;
			final HttpStatus        status;
			
			// Open connection to $(url)
			connection = (HttpURLConnection) url.openConnection();
			
			// Copy SEND_1 request headers to SEND_2 response headers
			for (final var key : exchange.getRequestHeaders().keySet()) {
				if (null == key) 
					continue;
				
				if (ArrayUtilities.containsStringIgnoreCase(ignoreHeaders, key)) 
					continue;
				
				connection.setRequestProperty(key, exchange.getRequestHeaders().getFirst(key));
			}
			
			// Handle method: read or write
			method = exchange.getRequestMethod();
			
			switch (method) {
				case GET: {
					connection.setDoInput(true);
					connection.setDoOutput(false);
					
					break;
				}
				case POST: {
					connection.setDoInput(true);
					connection.setDoOutput(true);
					
					try (final var stream = connection.getOutputStream()) {
						exchange.getRequestBody().transferTo(stream);
						
						stream.flush();
					}
					
					break;
				}
			}
			connection.setRequestMethod(method.name());
			
			// Get response status
			status = HttpStatus.forCode(connection.getResponseCode());
			
			if (status != HttpStatus.STATUS_OK) {
				errorMessage = connection.getResponseMessage();
				
				exchange.sendResponse(status, -1);
				
				throw new HttpException(status, errorMessage);
			}
			
			// Copy RECEIVE_2 headers to RECEIVE_1 headers
			for (final var key : connection.getHeaderFields().keySet()) {
				if (null == key) 
					continue;
				
				exchange.getResponseHeaders().add(key, connection.getHeaderField(key));
			}
			
			// Read response, if any
			try (final var stream = connection.getInputStream()) {
				response = stream.readAllBytes();
			}
			
			try {
				exchange.sendResponse(status, response.length);
				exchange.getResponseBody().write(response);
				exchange.getResponseBody().flush();
			} finally {
				if (closeOutput) try {
					exchange.getResponseBody().close();
				} catch (final IOException ex) {
					Exceptions.ignore(ex);
				}
			}
		} catch (final InvalidMethodException ex) {
			throw new Error(ex);
		}
	}
	
	
	
	private HttpUtilities (){
		throw new UnsupportedOperationException("Not supported.");
	}
}