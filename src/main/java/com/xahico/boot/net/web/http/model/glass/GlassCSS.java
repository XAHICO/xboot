/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.net.web.http.model.glass;

import java.util.HashMap;
import java.util.Map;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
final class GlassCSS {
	public static String prepareCSS (final String data){
		final Map<String, String> mappings;
		final StringBuilder       sb;
		
		sb = new StringBuilder();
		
		mappings = new HashMap<>();
		
		for (var i = 0; i < data.length(); i++) {
			char c;
			
			c = data.charAt(i);
			
			if (c == '$') {
				final StringBuilder kb;
				
				kb = new StringBuilder();
				
				for (i++; i < data.length(); i++) {
					c = data.charAt(i);
					
					if (c == ';') {
						final int    delimiter;
						final String line;

						line = sb.toString();

						delimiter = line.indexOf(':');

						if (delimiter != -1) {
							mappings.put(line.substring(0, delimiter).strip(), line.substring(delimiter + 1).strip());
						} else {
							sb.append('$');
							sb.append(kb);
							sb.append(';');
						}
						
						break;
					} else {
						kb.append(c);
					}
				}
				
				continue;
			}
			
			sb.append(c);
		}
		
		for (final var varkey : mappings.keySet()) {
			final String copy;
			
			copy = sb.toString();
			
			sb.delete(0, sb.length());
			sb.append(copy.replace(("$" + varkey), mappings.get(varkey)));
		}
		
		return sb.toString();
	}
	
	
	
	private GlassCSS (){
		throw new UnsupportedOperationException("Not supported yet.");
	}
}