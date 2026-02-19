/*
 * Written by Tuomas Kontiainen <https://www.github.com/tutomiko>
 * 
 * Copyright (c) 2023, Tuomas Kontiainen
 */
package com.xahico.boot.net.sock.model.fts;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import com.xahico.boot.pilot.ServiceType;

/**
 * Buffered Abstract Session Handler.
 * 
 * @author Tuomas Kontiainen
**/
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@ServiceType(FTSServiceProvider.class)
public @interface FTSService {
	int bufferSize () default 8196;
	
	int port () default 0;
}