/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.net.web.http.model.glass;

import com.xahico.boot.util.StringUtilities;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class GlassNavigationContext {
	public static GlassNavigationContext parseContext (final String pattern, final String path){
		final GlassNavigationContext context;
		final List<String>           pathParts;
		final List<String>           pattParts;
	
		context = new GlassNavigationContext();
		
		pattParts = StringUtilities.splitStringIntoList(pattern, "/", true);
		pathParts = StringUtilities.splitStringIntoList(path, "/", true);
		
		if (pathParts.size() != pattParts.size()) {
			throw new Error("'%s' not '%s'".formatted(path, pattern));
		}
		
		for (var i = 0; i < pattParts.size(); i++) {
			final String key;
			final String pathPart;
			final String pattPart;
			
			pattPart = pattParts.get(i);
			pathPart = pathParts.get(i);
			
			if (! pattPart.startsWith("$")) {
				continue;
			}
			
			if (pattPart.length() < 3) {
				throw new Error();
			}
			
			key = pattPart.substring(2, (pattPart.length() - 1));
			
			context.mappings.put(key.strip().toLowerCase(), pathPart);
		}
		
		return context;
	}
	
	
	
	private final Map<String, String> mappings = new HashMap<>();
	
	
	
	private GlassNavigationContext (){
		super();
	}
	
	
	
	public String get (final String key){
		return this.mappings.get(key.toLowerCase());
	}
}