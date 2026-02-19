/*
 * Written by Tuomas Kontiainen <https://www.github.com/tutomiko>
 * 
 * Copyright (c) 2023, Tuomas Kontiainen
 */
package com.xahico.boot.net.sock.model.trax;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import com.xahico.boot.pilot.ServiceType;

/**
 * TRAX (Transformative | Reactionary | Asynchronous | eXtensible) services 
 * are a high-intensity implementation of a single-threaded TCP/IP server 
 * with unique-to-kin "solo sessions" (where each connection will share their 
 * own vision of the class instance.) 
 * 
 * Harnessing the flexibility, transparency and universal nature of JSOX 
 * and combining it with the performance of socket channels for incomplete 
 * request management and on-demand response dispatch 
 * to bring together a vision of great power at bare-minimum cost.
 * 
 * @author Tuomas Kontiainen
**/
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@ServiceType(TRAXServiceProvider.class)
public @interface TRAXService {
	int bufferSize () default 512;
	
	boolean multithreaded () default false;
	
	int port () default 0;
}