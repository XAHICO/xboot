/*
 * Written by Tuomas Kontiainen <https://www.github.com/tutomiko>
 * 
 * Copyright (c) 2023, Tuomas Kontiainen
 */
package com.xahico.boot.platform;

import java.util.ArrayList;
import java.util.List;

/**
 * TBD.
 * 
 * @author hat
**/
public enum PlatformFamily {
	BSD		("BSD"),
	DARWIN	("Apple Mac/OSX"), // Changed from 'MACINTOSH' 
	LINUX		("Linux"), 
	WINDOWS	("Microsoft Windows"),
	UNIVERSAL	("Universal"), 
	UNIX		("Unix"),
	UNKNOWN	("Unknown");
	
	
	
	
	public static PlatformFamily forName (final String name) throws IllegalArgumentException {
		for (final var platformFamily : PlatformFamily.values()) {
			if (platformFamily.name().equalsIgnoreCase(name)) {
				return platformFamily;
			}
		}
		
		throw new IllegalArgumentException(name);
	}
	
	public static PlatformFamily forValue (final int ivalue) throws IllegalArgumentException {
		return PlatformFamily.forValue((byte) ivalue);
	}
	
	
	
	private final String displayString;
	
	
	
	PlatformFamily (final String displayString){
		this.displayString = displayString;
	}
	
	
	
	public List<PlatformModel> getModels (){
		final List<PlatformModel> collection;
		
		collection = new ArrayList<>();
		
		for (final var platformModel : PlatformModel.values()) {
			if ((this == UNIVERSAL) || (platformModel.getFamily() == this)) {
				collection.add(platformModel);
			}
		}
		
		return collection;
	}
	
	@Override
	public String toString (){
		return this.displayString;
	}
}