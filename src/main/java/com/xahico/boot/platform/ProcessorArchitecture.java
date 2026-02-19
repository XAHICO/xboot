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
public enum ProcessorArchitecture {
	AMD64		("AMD (x64)"), 
	ARM		("ARM (x86)"), 
	ARM64		("ARM (x64)"), 
	IA64		("Itanium (x64)"), 
	INTEL		("Intel (x86)"),
	UNIVERSAL	("Universal"),
	UNKNOWN	("Unknown");
	
	
	
	public static ProcessorArchitecture forName (final String name) throws IllegalArgumentException {
		for (final var processorArchitecture : ProcessorArchitecture.values()) {
			if (processorArchitecture.name().equalsIgnoreCase(name)) {
				return processorArchitecture;
			}
		}
		
		if (name.equalsIgnoreCase("x86")) 
			return INTEL;
		
		if (name.equalsIgnoreCase("x64")) 
			return AMD64;
		
		throw new IllegalArgumentException("");
	}
	
	public static List<ProcessorArchitecture> get32Bits (){
		final List<ProcessorArchitecture> collection;
		
		collection = new ArrayList<>();
		
		for (final var architecture : ProcessorArchitecture.values()) {
			if ((architecture == ProcessorArchitecture.UNIVERSAL) || (architecture == ProcessorArchitecture.UNKNOWN)) 
				continue;
			
			if (architecture.is32Bit()) {
				collection.add(architecture);
			}
		}
		
		return collection;
	}
	
	public static List<ProcessorArchitecture> get64Bits (){
		final List<ProcessorArchitecture> collection;
		
		collection = new ArrayList<>();
		
		for (final var architecture : ProcessorArchitecture.values()) {
			if ((architecture == ProcessorArchitecture.UNIVERSAL) || (architecture == ProcessorArchitecture.UNKNOWN)) 
				continue;
			
			if (architecture.is64Bit()) {
				collection.add(architecture);
			}
		}
		
		return collection;
	}
	
	
	
	private final String displayString;
	
	
	
	ProcessorArchitecture (final String displayString){
		this.displayString = displayString;
	}
	
	
	
	public boolean is32Bit (){
		switch (this) {
			case ARM:
			case INTEL:
			case UNIVERSAL:
				return true;
			default: {
				return false;
			}
		}
	}
	
	public boolean is64Bit (){
		switch (this) {
			case AMD64:
			case ARM64:
			case IA64:
			case UNIVERSAL:
				return true;
			default: {
				return false;
			}
		}
	}
	
	public ProcessorArchitecture to32Bit (){
		switch (this) {
			case AMD64: 
				return INTEL;
			case ARM64:
				return ARM;
			case IA64:
				return INTEL;
			default: {
				if (this.is32Bit())
					return ProcessorArchitecture.this;
				else {
					throw new InternalError("");
				}
			}
		}
	}
	
	public ProcessorArchitecture to64Bit (){
		switch (this) {
			case ARM:
				return ARM64;
			case INTEL:
				return IA64;
			default: {
				if (this.is64Bit())
					return ProcessorArchitecture.this;
				else {
					throw new InternalError("");
				}
			}
		}
	}
	
	@Override
	public String toString (){
		return this.displayString;
	}
}