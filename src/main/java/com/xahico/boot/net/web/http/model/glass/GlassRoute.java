/*
 * Written by Tuomas Kontiainen <https://www.github.com/tutomiko>
 * 
 * Copyright (c) 2023, Tuomas Kontiainen
 */
package com.xahico.boot.net.web.http.model.glass;

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
@Target(ElementType.METHOD)
public @interface GlassRoute {
	int ACCESS_PRIVATE = (1 << 1);
	int ACCESS_PUBLIC = (1 << 2);
	
	
	
	int accessible () default 0;
	
	String path ();
	
	String target () default "";
	
	GlassRouteType type ();
}