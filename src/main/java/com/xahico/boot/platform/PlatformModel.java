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
 * @author Tuomas Kontiainen
**/
public enum PlatformModel {
	// BSD
	FREEBSD			("FreeBSD", PlatformFamily.BSD), 
	NETBSD			("NetBSD", PlatformFamily.BSD), 
	
	// Linux
	ANDROID			("Android", PlatformFamily.LINUX), 
	BACKTRACK			("BackTrack", PlatformFamily.LINUX), 
	CHROME_OS			("Chrome OS", PlatformFamily.LINUX), 
	DEBIAN			("Debian", PlatformFamily.LINUX), 
	FEDORA			("Fedora", PlatformFamily.LINUX), 
	GENTOO			("Gentoo Linux", PlatformFamily.LINUX), 
	KALI				("Kali Linux", PlatformFamily.LINUX), 
	MIKROTIK			("RouterOS", PlatformFamily.LINUX),
	TAILS				("TAILS", PlatformFamily.LINUX), 
	UBUNTU			("Ubuntu", PlatformFamily.LINUX), 
	
	// Darwin (MacIntosh, Mac/OSX)
	OSX				("OSX", PlatformFamily.DARWIN), 
	
	// Microsoft Windows
	WINDOWS_7			("Windows 7", PlatformFamily.WINDOWS), 
	WINDOWS_8			("Windows 8", PlatformFamily.WINDOWS), 
	WINDOWS_8_1			("Windows 8.1", PlatformFamily.WINDOWS), 
	WINDOWS_10			("Windows 10", PlatformFamily.WINDOWS), 
	WINDOWS_11			("Windows 11", PlatformFamily.WINDOWS), 
	WINDOWS_NT			("Windows 2000 or earlier", PlatformFamily.WINDOWS), 
	WINDOWS_SERVER_2003	("Windows Server 2003", PlatformFamily.WINDOWS),
	WINDOWS_SERVER_2008	("Windows Server 2008", PlatformFamily.WINDOWS),
	WINDOWS_SERVER_2012	("Windows Server 2012", PlatformFamily.WINDOWS),
	WINDOWS_SERVER_2016	("Windows Server 2016", PlatformFamily.WINDOWS),
	WINDOWS_SERVER_2019	("Windows Server 2019", PlatformFamily.WINDOWS),
	WINDOWS_SERVER_2022	("Windows Server 2022", PlatformFamily.WINDOWS),
	WINDOWS_VISTA		("Windows Vista", PlatformFamily.WINDOWS), 
	WINDOWS_XP			("Windows XP", PlatformFamily.WINDOWS), 
	XBOX_360_DB			("Xbox 360", PlatformFamily.WINDOWS), 
	XBOX_OS			("Xbox 1", PlatformFamily.WINDOWS), 
	
	// Unix
	ONTAP				("ONTAP", PlatformFamily.UNIX),
	PLAYSTATION_3		("Cell OS", PlatformFamily.UNIX), 
	PLAYSTATION_4		("Orbis OS", PlatformFamily.UNIX),
	
	UNKNOWN			("Unknown", null); 
	
	
	
	public static List<PlatformModel> collectSupported (final PlatformFamily platform, final List<PlatformModel> selection){
		final List<PlatformModel> collection;
		
		collection = new ArrayList<>();
		
		if (platform == PlatformFamily.UNIVERSAL) {
			collection.addAll(selection);
		} else for (final var model : selection) {
			if (model.getFamily() == platform) {
				collection.add(model);
			}
		}
		
		return collection;
	}
	
	public static PlatformModel forName (final String name) throws IllegalArgumentException {
		if (name.contains("  ")) {
			return forName(name.replaceAll("\\s+", " "));
		}
		
		for (final var platformModel : PlatformModel.values()) {
			if (platformModel.name().equalsIgnoreCase(name)) {
				return platformModel;
			}
			
			if (platformModel.toString().equalsIgnoreCase(name)) {
				return platformModel;
			}
		}
		
		if (name.contains(" ")) {
			return forName(name.replace(' ', '_'));
		}
		
		throw new IllegalArgumentException(String.format("No such platform model: %s", name));
	}
	
	public static PlatformModel[] parseString (final String platformModelsString){
		final PlatformModel[] platformModels;
		final String[]        platformModelsIterableArray;
		final String          platformModelsIterableString;
		
		if (platformModelsString.length() < 2) 
			return new PlatformModel[0];
		
		platformModelsIterableString = platformModelsString.substring(1, platformModelsString.length() - 1);
		
		if (platformModelsIterableString.isEmpty() || platformModelsIterableString.isBlank()) 
			return new PlatformModel[0];
		
		platformModelsIterableArray = platformModelsIterableString.split("\\s*,\\s*");
		platformModels = new PlatformModel[platformModelsIterableArray.length];
		
		for (var i = 0; i < platformModelsIterableArray.length; i++) {
			final PlatformModel platformModel;
			final String        platformModelString;
			
			platformModelString = platformModelsIterableArray[i];
			
			platformModel = PlatformModel.forName(platformModelString);
			
			platformModels[i] = platformModel;
		}
		
		return platformModels;
	}
	
	public static String toString (final PlatformModel[] platformModels){
		final StringBuilder sb;
		
		sb = new StringBuilder();
		sb.append('[');
		
		for (var i = 0; i < platformModels.length; i++) {
			final PlatformModel platformModel;
			
			platformModel = platformModels[i];
			
			sb.append(platformModel.name());
			
			if ((i + 1) < platformModels.length) {
				sb.append(',');
				sb.append(' ');
			}
		}
		
		sb.append(']');
		
		return sb.toString();
	}
	
	
	
	private final PlatformFamily family;
	private final String         fullName;
	
	
	
	PlatformModel (final String fullName, final PlatformFamily family){
		this.fullName = fullName;
		this.family = family;
	}
	
	
	
	public PlatformFamily getFamily (){
		return this.family;
	}
	
	@Override
	public String toString (){
		return this.fullName;
	}
}