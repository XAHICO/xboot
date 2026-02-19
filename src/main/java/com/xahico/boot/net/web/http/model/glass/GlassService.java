/*
 * Written by Tuomas Kontiainen <https://www.github.com/tutomiko>
 * 
 * Copyright (c) 2023, Tuomas Kontiainen
 */
package com.xahico.boot.net.web.http.model.glass;

import com.xahico.boot.pilot.ServiceType;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@ServiceType(GlassServiceProvider.class)
public @interface GlassService {
	String authToken () default "";
	
	String defaultPath () default "";
	
	String[] endorsements () default "";
	
	Class<? extends GlassEvent> eventClass () default GlassEvent.class;
	
	boolean eventHandler () default false;
	
	boolean fileServer () default false;
	
	boolean multithreaded () default false;
	
	int port () default 0;
	
	Class<? extends GlassSession> sessionClass () default GlassSession.class;
	
	Class<? extends Enum> statusClass ();
}