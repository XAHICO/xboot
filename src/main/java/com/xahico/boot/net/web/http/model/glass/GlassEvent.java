/*
 * Written by Tuomas Kontiainen <https://www.github.com/tutomiko>
 * 
 * Copyright (c) 2023, Tuomas Kontiainen
 */
package com.xahico.boot.net.web.http.model.glass;

import com.xahico.boot.event.Event;
import com.xahico.boot.lang.jsox.JSOXObject;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public abstract class GlassEvent extends JSOXObject implements Event {
	public static String getEventCodeClass (final Class<? extends GlassEvent> jclass){
		final GlassEventMarkup markup;
		
		markup = jclass.getAnnotation(GlassEventMarkup.class);
		
		if (null == markup) {
			throw new Error(String.format("invalid %s: '%s' not annotated with %s", GlassEvent.class, jclass, GlassEventMarkup.class));
		}
		
		return jclass.getSimpleName();
	}
	
	
	
	protected GlassEvent (){
		super();
	}
	
	
	
	public final String eventCode (){
		return getEventCodeClass(this.getClass());
	}
}