/*
 * Written by Tuomas Kontiainen <https://www.github.com/tutomiko>
 * 
 * Copyright (c) 2023, Tuomas Kontiainen
 */
package com.xahico.boot.net.sock.model.lobex;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import com.xahico.boot.pilot.ServiceType;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@ServiceType(LOBEXServiceProvider.class)
public @interface LOBEXService {
	int bufferSize () default 512;
	
	Class<? extends LOBEXEvent> eventClass ();
	
	int port () default 0;
	
	boolean singleton () default false;
}