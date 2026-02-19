/*
 * Written by Tuomas Kontiainen <https://www.github.com/tutomiko>
 * 
 * Copyright (c) 2023, Tuomas Kontiainen
 */
package com.xahico.boot.net.sock.model.bash;

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
@ServiceType(BASHServiceProvider.class)
public @interface BASHService {
	int bufferSize () default 512;
	
	int port () default 0;
	
	boolean useDynamicBuffers () default false;
}