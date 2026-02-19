/*
 * Written by Tuomas Kontiainen <https://www.github.com/tutomiko>
 * 
 * Copyright (c) 2023, Tuomas Kontiainen
 */
package com.xahico.boot.net.web.http.model.glass;

import com.xahico.boot.net.URIA;
import com.xahico.boot.reflection.ReflectionMethod;
import com.xahico.boot.util.StringUtilities;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
final class GlassRoutingMethod {
	private final ReflectionMethod methodHandle;
	private final GlassRoute       route;
	
	
	
	GlassRoutingMethod (final ReflectionMethod methodHandle, final GlassRoute route){
		super();
		
		this.methodHandle = methodHandle;
		this.route = route;
	}
	
	
	
	public String getPath (){
		return this.route.path();
	}
	
	public String getTarget (){
		return this.route.target();
	}
	
	public void invoke (final Object instance, final GlassNavigationContext context, final URIA uri, final GlassResource resource) throws ExecutionException {
		this.methodHandle.invokeDirect(instance, new Object[]{context, uri, resource});
	}
	
	public boolean isPrivateAccessible (){
		return ((this.route.accessible() & GlassRoute.ACCESS_PRIVATE) != 0);
	}
	
	public boolean isPublicAccessible (){
		return ((this.route.accessible() & GlassRoute.ACCESS_PUBLIC) != 0);
	}
	
	public boolean matches (final String path){
		final boolean      joker;
		final List<String> pathParts;
		final List<String> pattParts;
		
		pattParts = StringUtilities.splitStringIntoList(this.getPath(), "/", true);
		pathParts = StringUtilities.splitStringIntoList(path, "/", true);
		
		if (! pattParts.isEmpty()) 
			joker = pattParts.get(pattParts.size() - 1).equals("*");
		else {
			joker = false;
		}
		
		if (joker) {
			if (pathParts.size() < pattParts.size()) {
				return false;
			}
		} else {
			if (pathParts.size() != pattParts.size()) {
				return false;
			}
		}
		
		if (pattParts.isEmpty() && !pathParts.isEmpty()) 
			return false;
		
		for (var i = 0; i < pattParts.size(); i++) {
			final String pathPart;
			final String pattPart;
			
			pattPart = pattParts.get(i);
			pathPart = pathParts.get(i);
			
			if (pattPart.isBlank() != pathPart.isBlank()) 
				return false;
			
			if (!pattPart.equals("*") && !pattPart.startsWith("$") && !pattPart.equalsIgnoreCase(pathPart)) {
				return false;
			}
		}
		
		return true;
	}
	
	public GlassRouteType type (){
		return this.route.type();
	}
}