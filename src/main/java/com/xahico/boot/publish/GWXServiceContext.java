/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.publish;

import com.xahico.boot.reflection.ClassFactory;
import io.netty.util.AttributeKey;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class GWXServiceContext {
	public Executor                             executor = null;
	public final GWXNamespace                   globalNamespace = new GWXNamespace();
	public final ClassFactory<GWXSession>       instanceFactory;
	public GWXAPIInterfaceManager               interfaceManager = null;
	public final AttributeKey<String>           keyAuth = AttributeKey.valueOf("auth");
	public final AttributeKey<GWXEventListener> keyListener = AttributeKey.valueOf("websocket");
	public final AttributeKey<GWXSession>       keySession = AttributeKey.valueOf("session");
	public GWXResourceManager                   rcm = null;
	public final Map<String, GWXSession>        sessions = new ConcurrentHashMap<>();
	public int                                  sessionTimeout = 0;
	public boolean                              useSSL = false;
	public GWXWebInterface                      webInterface = null;
	public GWXNamespace                         webNamespace = null;
	
	
	
	GWXServiceContext (final GWXServiceProvider service, final ClassFactory<GWXSession> instanceFactory){
		super();
		
		this.instanceFactory = instanceFactory;
	}
}