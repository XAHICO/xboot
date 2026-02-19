/*
 * Written by Tuomas Kontiainen <https://www.github.com/tutomiko>
 * 
 * Copyright (c) 2023, Tuomas Kontiainen
 */
package com.xahico.boot.synchronicity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to annotate methods of a 
 * {@link com.xahico.boot.synchronicity.Synchronizable synchronizable class} 
 * for implicit automated synchronization. 
 * <br>
 * <br>
 * Caution: 
 * <br>
 * improper use may result in deadlock due to implicit synchronization.
 * Do NOT annotate 
 * {@link com.xahico.boot.pilot.ServiceProvider service provider} 
 * superclass overrides, e.g. the 'onCreate()' of 
 * {@link com.xahico.boot.net.Session}.
 * 
 * @author Tuomas Kontiainen
**/
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Synchronized {
	
}