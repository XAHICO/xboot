/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.publish.handlers;

import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponse;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class GWXHttpContentCompressor extends HttpContentCompressor {
	@Override
	protected Result beginEncode (final HttpResponse headers, final String acceptEncoding) throws Exception{
		final String contentType;
		
		contentType = headers.headers().get(HttpHeaderNames.CONTENT_TYPE);
		
		if ((contentType != null) && contentType.startsWith("image/")) {
		    return null; // No compression
		}
		
		return super.beginEncode(headers, acceptEncoding);
	}
}