/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.platform;

import com.xahico.boot.util.ArrayUtilities;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class ProcessUtilities {
	public static String[] buildArguments (final Map<String, String> argsMap){
		final String[]         argsArray;
		final Iterator<String> it;
		
		argsArray = new String[argsMap.size()];
		
		it = argsMap.keySet().iterator();
		
		for (var i = 0; it.hasNext();) {
			final String key;
			final String value;
			
			key = it.next();
			value = argsMap.get(key);
			
			argsArray[i] = key;
			i++;
			
			argsArray[i] = value;
			i++;
		}
		
		return argsArray;
	}
	
	public static String[] buildCommandline (final String applicationName, final String... args){
		final String[] commandline;
		
		commandline = new String[1 + args.length];
		commandline[0] = applicationName;
		
		ArrayUtilities.putAll(commandline, 1, args);
		
		return commandline;
	}
	
	public static Process createProcess (final File applicationFile, final String... args) throws IOException {
		return createProcess(applicationFile.getAbsolutePath(), args);
	}
	
	public static Process createProcess (final Path applicationFilePath, final String... args) throws IOException {
		return createProcess(applicationFilePath.toFile(), args);
	}
	
	public static Process createProcess (final String applicationName, final String... args) throws IOException {
		final String[] commandline;
		final Process  process;
		
		commandline = buildCommandline(applicationName, args);
		
		process = createProcess(commandline, true);
		
		return process;
	}
	
	public static Process createProcess (final File applicationFile, final List<String> args) throws IOException {
		return createProcess(applicationFile.getAbsolutePath(), args);
	}
	
	public static Process createProcess (final Path applicationFilePath, final List<String> args) throws IOException {
		return createProcess(applicationFilePath.toFile(), args);
	}
	
	public static Process createProcess (final String applicationName, final List<String> args) throws IOException {
		return createProcess(applicationName, args.toArray(new String[args.size()]));
	}
	
	public static Process createProcess (final List<String> commandline, final boolean inheritIO) throws IOException {
		final Process        process;
		final ProcessBuilder processBuilder;
		
		processBuilder = new ProcessBuilder();
		processBuilder.command(commandline);
		
		if (inheritIO) {
			processBuilder.inheritIO();
		}
		
		process = processBuilder.start();
		
		return process;
	}
	
	public static Process createProcess (final String[] commandline, final boolean inheritIO) throws IOException {
		final Process        process;
		final ProcessBuilder processBuilder;
		
		processBuilder = new ProcessBuilder();
		processBuilder.command(commandline);
		
		if (inheritIO) {
			processBuilder.inheritIO();
		}
		
		process = processBuilder.start();
		
		return process;
	}
	
	
	
	private ProcessUtilities (){
		throw new UnsupportedOperationException("Not supported.");
	}
}