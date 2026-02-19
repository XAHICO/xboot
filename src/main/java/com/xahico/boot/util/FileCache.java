/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public abstract class FileCache {
	public static FileCache createConcurrentTextFileCache (){
		return createMapBackedTextFileCache(new ConcurrentHashMap<>());
	}
	
	public static FileCache createMapBackedTextFileCache (final Map<File, Entry> backing){
		return new FileCache() {
			@Override
			public void clear (){
				backing.clear();
			}
			
			@Override
			public String load (final File file){
				final Entry entry;
				
				entry = backing.computeIfAbsent(file, (__) -> new Entry());
				
				if (file.lastModified() > entry.from) try {
					entry.data = Files.readString(file.toPath());
					entry.from = file.lastModified();
				} catch (final IOException ex) {
					return null;
				}
				
				return entry.data;
			}
		};
	}
	
	
	
	public FileCache (){
		super();
	}
	
	
	
	public abstract void clear ();
	
	public abstract String load (final File file);
	
	
	
	public static final class Entry {
		public String data;
		public long   from = -1;
		
		
		
		public Entry (){
			super();
		}
	}
}