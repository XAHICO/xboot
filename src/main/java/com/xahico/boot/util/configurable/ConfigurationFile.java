/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.xahico.boot.util.configurable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public class ConfigurationFile implements Configuration {
	private static final Pattern PATTERN_PROPERTY = Pattern.compile("([A-Za-z][A-Za-z0-9\\_]*)\\s*\\:\\s*(.+)");
	
	
	
	/** TBD. **/
	private String data = null;
	
	/** TBD. **/
	private final File file;
	
	/** TBD. **/
	private Map<String, Configuration.Property> properties = null;
	
	
	
	public ConfigurationFile (final File file) throws IOException {
		this.file = file;
		this.reload();
	}
	
	public ConfigurationFile (final Path path) throws IOException {
		this(path.toFile());
	}
	
	public ConfigurationFile (final String filename) throws IOException {
		this(new File(filename));
	}
	
	
	
	@Override
	public Configuration.Property getProperty (final String key) throws NoSuchPropertyException {
		if (key.matches(".*[A-Z].*")) 
			return this.getProperty(key.toLowerCase());
		else if (properties.containsKey(key)) 
			return properties.get(key);
		else {
			throw new NoSuchPropertyException(key);
		}
	}
	
	/**
	 * TBD.
	 * 
	 * @return 
	 * TBD
	**/
	public final File getFile (){
		return this.file;
	}
	
	@Override
	public Set<String> properties (){
		return this.properties.keySet();
	}
	
	/**
	 * TBD.
	 * 
	 * @throws FileNotFoundException 
	 * TBD
	 * 
	 * @throws IOException 
	 * TBD
	**/
	public final synchronized void reload () throws FileNotFoundException, IOException {
		// ...
		data = Files.readString(file.toPath());
		
		// ...
		properties = new HashMap<>();
		
		try (final Stream<String> lines = data.lines()) {
			lines
				.filter((final String s) -> !s.startsWith("#"))
				.forEach((final String line) -> {
				
				final var matcher = PATTERN_PROPERTY.matcher(line);
				
				if (matcher.matches()) {
					final var key = matcher.group(1);
					final var value = matcher.group(2);
					
					properties.put(key.toLowerCase(), new Configuration.Property(key, value));
				}
			});
		}
	}
	
	/**
	 * TBD.
	 * 
	 * @throws IOException 
	 * TBD
	**/
	public final synchronized void save () throws IOException {
		final List<String> temp = new LinkedList<>();
		
		try (final Stream<String> lines = data.lines()) {
			lines.forEach((final String line) -> {
				if (line.matches("\\s*#")) 
					temp.add(line);
				else {
					final var matcher = PATTERN_PROPERTY.matcher(line);
					
					if (matcher.matches()) {
						final var key = matcher.group(1);
						final var value = matcher.group(2);
						
//						temp.add(line.replace(value, properties.get(key.toLowerCase()).get()));
						temp.add(String.format("%s: %s", key, properties.get(key.toLowerCase()).get()));
					} else {
						temp.add(line);
					}
				}
			});
		}
		
		try (final var fout = new FileWriter(file, false)) {
			for (final var line : temp) {
				fout.write(line);
				fout.write(System.lineSeparator());
			}
		}
	}
	
	@Override
	public void setProperty (final String key, final String value) throws NoSuchPropertyException {
		this.getProperty(key).set(value);
	}
}