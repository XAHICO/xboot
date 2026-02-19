/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.util;

import com.xahico.boot.dev.Helper;
import com.xahico.boot.lang.jsox.JSOXObject;
import com.xahico.boot.lang.jsox.JSOXVariant;
import com.xahico.boot.platform.FileUtilities;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class ZipUtilities {
	private static final int        DEFAULT_BUFFER_SIZE = 1024;
	private static final ZipHandler DEFAULT_INFLATION_HANDLER = (entry, path) -> true;
	
	
	
	public static byte[] createDefaultBuffer (){
		return new byte[DEFAULT_BUFFER_SIZE];
	}
	
	@Helper
	public static Collection<Path> unzipFile (final File inputArchive, final File outputDirectory) throws IOException {
		return unzipFile(inputArchive.toPath(), outputDirectory.toPath(), DEFAULT_INFLATION_HANDLER);
	}
	
	@Helper
	public static Collection<Path> unzipFile (final Path inputArchivePath, final Path outputDirectoryPath, final ZipHandler handler) throws IOException {
		return unzipFile(inputArchivePath, outputDirectoryPath, createDefaultBuffer(), handler);
	}
	
	@SuppressWarnings("ThrowFromFinallyBlock")
	public static Collection<Path> unzipFile (final Path inputArchivePath, final Path outputDirectoryPath, final byte[] buffer, final ZipHandler handler) throws IOException {
		final List<Path> collection;
		
		collection = new ArrayList<>();
		
		try (final var stream = new ZipInputStream(Files.newInputStream(inputArchivePath))) {
			ZipEntry entry;
			
			entry = stream.getNextEntry();
			
			while (entry != null) {
				final Path pathInDirectory;
				
				pathInDirectory = Paths.get(outputDirectoryPath.toString(), entry.getName());
				
				if (! handler.accept(entry, pathInDirectory)) 
					continue;
				
				if (entry.isDirectory()) {
					if (!Files.exists(pathInDirectory)) {
						Files.createDirectories(pathInDirectory);
						
						collection.add(pathInDirectory);
					}
				} else {
					if (!pathInDirectory.getParent().equals(outputDirectoryPath) && !Files.exists(pathInDirectory.getParent())) {
						Files.createDirectories(pathInDirectory.getParent());
						
						collection.add(pathInDirectory.getParent());
					}
					
					try (final var destination = Files.newOutputStream(pathInDirectory)) {
						int length;
						
						while ((length = stream.read(buffer)) > 0) {
							destination.write(buffer, 0, length);
						}
					}
					
					collection.add(pathInDirectory);
				}
				
				entry = stream.getNextEntry();
			}
		} catch (final IOException ex) {
			try {
				for (final var path : collection) {
					FileUtilities.delete(path);
				}
				
				collection.clear();
			} finally {
				throw ex;
			}
		}
		
		return collection;
	}
	
	public static byte[] zipDirectory (final File inputDirectory) throws IOException {
		return zipDirectory(inputDirectory.toPath());
	}
	
	public static byte[] zipDirectory (final Path inputDirectoryPath) throws IOException {
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
	public static void zipDirectory (final File inputDirectory, final File outputFile) throws IOException {
		zipDirectory(inputDirectory.toPath(), outputFile.toPath());
	}
	
	@SuppressWarnings("ThrowFromFinallyBlock")
	public static void zipDirectory (final Path inputDirectoryPath, final Path outputFilePath) throws IOException {
		final AtomicReference<IOException> thrownException;
		
		thrownException = new AtomicReference<>(null);
		
		try (final var stream = new ZipOutputStream(Files.newOutputStream(outputFilePath))) {
			Files.walk(inputDirectoryPath)
			     .filter(__ -> (null == thrownException.get()))
			     .filter(path -> !Files.isDirectory(path))
			     .forEach(path -> {
				try {
					final ZipEntry entry;
					
					entry = new ZipEntry(inputDirectoryPath.relativize(path).toString());
					
					stream.putNextEntry(entry);
					
					try {
						Files.copy(path, stream);
					} catch (final NoSuchFileException ex) {
						Exceptions.ignore(ex);
					}
					
					stream.closeEntry();
				} catch (final IOException ex) {
					thrownException.set(ex);
				}
			});
		}
		
		if (null != thrownException.get()) try {
			FileUtilities.delete(outputFilePath);
		} finally {
			throw thrownException.get();
		}
	}
	
	
	
	private ZipUtilities (){
		throw new UnsupportedOperationException("Not supported.");
	}
}