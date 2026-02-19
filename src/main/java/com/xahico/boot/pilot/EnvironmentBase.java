/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.pilot;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public abstract class EnvironmentBase {
	private static String location = "./";
	
	
	
	public static final synchronized String getLocation (){
		return EnvironmentBase.location;
	}
	
	protected static final File getFile (final String... names){
		return getPath(names).toFile();
	}
	
	protected static final Path getPath (final String... names){
		return Paths.get(getLocation(), names);
	}
	
	public static final synchronized void initLocation (final String location){
		EnvironmentBase.location = location;
	}
	
	
	
	protected EnvironmentBase (){
		throw new UnsupportedOperationException("Not supported");
	}
}