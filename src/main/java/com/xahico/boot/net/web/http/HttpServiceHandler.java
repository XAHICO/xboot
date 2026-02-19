/*
 * Written by Tuomas Kontiainen <https://www.github.com/tutomiko>
 * 
 * Copyright (c) 2023, Tuomas Kontiainen
 */
package com.xahico.boot.net.web.http;

import java.io.IOException;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public interface HttpServiceHandler {
	public void handle (final HttpServiceEnvironment env, final HttpServiceClient client, final HttpServiceExchange exchange) throws Exception;
}