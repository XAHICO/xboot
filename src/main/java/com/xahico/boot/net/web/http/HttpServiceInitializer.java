/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.net.web.http;

import com.sun.net.httpserver.HttpsServer;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
@FunctionalInterface
public interface HttpServiceInitializer {
	HttpServiceInitializer DEFAULT = (server) -> {};
	
	
	
	void initialize (final HttpsServer server);
}