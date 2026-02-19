/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.net.web.http.model.glass;

import com.xahico.boot.util.StringUtilities;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class GlassTheme {
	public static GlassTheme loadThemeFromFile (final File file) throws IOException {
		final Map<String, String> mappings;
		
		mappings = new HashMap<>();
		
		for (final String line : Files.readAllLines(file.toPath())) {
			final int    delimiter;
			final String key;
			boolean      valid = true;
			final String value;
			
			if (line.isBlank()) 
				continue;
			
			delimiter = line.indexOf(':');
			
			if (delimiter == -1) 
				continue;
			
			key = line.substring(0, delimiter).strip();
			value = line.substring(delimiter + 1).strip();
			
			for (final var ekey : Key.values()) {
				if (ekey.reference.equalsIgnoreCase(key)) {
					valid = true;
					
					break;
				}
			}
			
			if (valid) {
				mappings.put(key.toLowerCase(), (StringUtilities.isQuoted(value) ? StringUtilities.unquote(value) : value));
			}
		}
		
		return new GlassTheme(mappings);
	}
	
	
	
	private final Map<String, String> mappings;
	
	
	
	GlassTheme (final Map<String, String> mappings){
		super();
		
		this.mappings = mappings;
	}
	
	
	
	public void mapTo (final GlassNamespace namespace){
		for (final var key : this.mappings.keySet()) {
			namespace.set(key, this.mappings.get(key));
		}
	}
	
	
	
	public static enum Key {
		BACKGROUND_1("theme-bg-1"),
		BACKGROUND_2("theme-bg-2"),
		BORDER_1("theme-br-1"),
		BORDER_2("theme-br-2"),
		FOREGROUND_1("theme-fg-1"),
		FOREGROUND_2("theme-fg-2"),
		HIGHTLIGHT_1("theme-hl-1"),
		HIGHTLIGHT_2("theme-hl-2"),
		SUPER("theme-su");
		
		
		
		private final String reference;
		
		
		
		Key (final String reference){
			this.reference = reference;
		}
	}
}