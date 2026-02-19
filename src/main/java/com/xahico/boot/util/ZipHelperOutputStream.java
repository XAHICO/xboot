/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.util;

import com.xahico.boot.lang.jsox.JSOXObject;
import com.xahico.boot.lang.jsox.JSOXVariant;
import com.xahico.boot.platform.FileUtilities;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public class ZipHelperOutputStream extends ZipOutputStream {
	public ZipHelperOutputStream (final OutputStream stream){
		super(stream);
	}
	
	
	
	public void writeBytes (final String entryPath, final byte[] data) throws IOException {
		final ZipEntry entry;
		
		entry = new ZipEntry(entryPath);
		
		this.putNextEntry(entry);
		
		try {
			this.write(data);
		} finally {
			this.closeEntry();
		}
	}
	
	public void writeDirectory (final String entryPath, final File directory) throws IOException {
		this.writeDirectory(entryPath, directory.toPath());
	}
	
	public void writeDirectory (final String entryPath, final Path directoryPath) throws IOException {
		final AtomicReference<IOException> thrownException;
		
		thrownException = new AtomicReference<>(null);
		
		try (final var files = Files.walk(directoryPath, 1)) {
			files.filter(__ -> (null == thrownException.get())).filter(path -> !path.equals(directoryPath)).forEach(path -> {
				try {
					this.writeFile(Paths.get(entryPath, FileUtilities.pathIn(path, directoryPath).toString()).toString(), path);
				} catch (final IOException ex) {
					thrownException.set(ex);
				}
			});
		}
		
		if (null != thrownException.get()) {
			throw thrownException.get();
		}
	}
	
	public void writeFile (final String entryPath, final File file) throws IOException {
		this.writeFile(entryPath, file.toPath());
	}
	
	public void writeFile (final String entryPath, final Path filePath) throws IOException {
		if (Files.isDirectory(filePath)) {
			writeDirectory(entryPath, filePath);
		} else {
			final ZipEntry entry;
			
			entry = new ZipEntry(entryPath);
			
			this.putNextEntry(entry);
			
			try (final var source = Files.newInputStream(filePath)) {
				source.transferTo(this);
			} finally {
				this.closeEntry();
			}
		}
	}
	
	public void writeObject (final String entryPath, final JSOXObject object) throws IOException {
		this.writeString(entryPath, object.toJSONString());
	}
	
	public void writeObject (final String entryPath, final JSOXVariant object) throws IOException {
		this.writeString(entryPath, object.toJSONString());
	}
	
	public void writeString (final String entryPath, final String data) throws IOException {
		this.writeBytes(entryPath, data.getBytes());
	}
	
	public void writeString (final String entryPath, final String data, final Charset charset) throws IOException {
		this.writeBytes(entryPath, data.getBytes(charset));
	}
}