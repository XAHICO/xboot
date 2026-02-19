/*
 * Written by Tuomas Kontiainen <https://www.github.com/tutomiko>
 * 
 * Copyright (c) 2023, Tuomas Kontiainen
 */
package com.xahico.boot.logging;

import com.xahico.boot.dev.Helper;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.TimeZone;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class Logger {
	private static final String   FORMAT_DATE_TIME = "dd/MM/yyyy, HH:mm:ss";
	private static final TimeZone TIMEZONE_LOCAL = TimeZone.getDefault();
	private static final ZoneId   ZONE_LOCAL = ZoneId.systemDefault();
	
	
	
	private static String dtsNow (){
		final Date       date;
		final DateFormat format;
		final long       timeMillis;
		final String     timeString;
		
		// ...
		timeMillis = ZonedDateTime.now(ZONE_LOCAL).toInstant().toEpochMilli();
		
		date = new Date(timeMillis);

		format = new SimpleDateFormat(FORMAT_DATE_TIME);
		format.setTimeZone(TIMEZONE_LOCAL);
		
		timeString = format.format(date);
		
		return timeString;
	}
	
	public static Logger getLogger (final Class<?> jclass){
		return new Logger(jclass);
	}
	
	
	
	private final ThreadLocal<Logger.Entry> currentEntry = new ThreadLocal<>();
	private final Class<?>                  jclass;
	private OutputStream                    stream = null;
	
	
	
	private Logger (final Class<?> jclass){
		super();
		
		this.jclass = jclass;
	}
	
	
	
	private String buildHeader (){
		final StringBuilder sb;
		
		sb = new StringBuilder();
		sb.append(dtsNow());
		sb.append(System.lineSeparator());
		sb.append(this.getClassName());
		
		return sb.toString();
	}
	
	public Logger.Entry currentEntry (final boolean createIfAbsent){
		if (null != this.currentEntry.get()) {
			final Logger.Entry entry;
			
			entry = this.currentEntry.get();
			entry.handleCount++;
			
			return entry;
		} else if (createIfAbsent) {
			return this.newEntry();
		} else {
			return null;
		}
	}
	
	public String getClassName (){
		return this.jclass.getName();
	}
	
	public void log (final String message){
		this.logInternal(message, true);
	}
	
	public void log (final Throwable throwable){
		throwable.printStackTrace(new PrintWriter(stream));
	}
	
	public void log (final Throwable throwable, final String message){
		if (null != message) {
			this.log(message);
		}
		
		if (null != throwable) {
			this.log(throwable);
		}
	}
	
	private synchronized void logInternal (final String message, final boolean pad){
		try {
			final String body;
			final String head;
			
			head = buildHeader();
			body = message;
			
			stream.write(head.getBytes());
			stream.write("\n".getBytes());
			stream.write(body.getBytes());
			stream.write("\n".repeat(pad ? 3 : 2).getBytes());
			stream.flush();
		} catch (final IOException ex) {
			throw new Error(ex);
		}
	}
	
	public Logger.Entry newEntry (){
		return this.newEntry(false);
	}
	
	public Logger.Entry newEntry (final boolean ctransient){
		final Logger.Entry entry;
		
		if (! ctransient) {
			if (null != this.currentEntry.get()) 
				throw new IllegalStateException("Can only create one entry at a time per thread; an entry is already associated with this logger and the current thread");
		}
		
		entry = new Logger.Entry();
		
		if (! ctransient) {
			this.currentEntry.set(entry);
		}
		
		return entry;
	}
	
	@Helper
	public Logger setStream (final File file){
		try {
			if (! file.exists()) 
				file.createNewFile();
			
			this.setStream(new FileOutputStream(file, true));
		} catch (final FileNotFoundException ex) {
			throw new InternalError(ex);
		} catch (final IOException ex) {
			throw new Error(ex);
		}
		
		return Logger.this;
	}
	
	public Logger setStream (final OutputStream stream){
		this.stream = stream;
		
		return Logger.this;
	}
	
	
	
	public final class Entry implements AutoCloseable {
		private final StringBuilder data = new StringBuilder();
		private int                 handleCount = 0;
		
		
		
		private Entry (){
			super();
		}
		
		
		
		@Override
		public void close (){
			synchronized (Logger.this) {
				if (this.handleCount > 0) {
					this.handleCount--;
				} else try {
					if (! data.isEmpty()) {
						Logger.this.logInternal(Logger.Entry.this.data.toString(), (Logger.Entry.this.data.charAt(Logger.Entry.this.data.length() - 1) != '\n'));
					}
				} finally {
					Logger.this.currentEntry.remove();
				}
			}
		}
		
		public Logger.Entry write (final String string){
			this.data.append(string);
			
			return Logger.Entry.this;
		}
		
		public Logger.Entry write (final Throwable throwable){
			try (final var stream = new ByteArrayOutputStream()) {
				try (final var printer = new PrintWriter(stream)) {
					throwable.printStackTrace(printer);
				}
				
				return this.write(stream.toString());
			} catch (final IOException ex) {
				throw new Error(ex);
			}
		}
		
		public Logger.Entry writeLine (){
			this.data.append("\n");
			
			return Logger.Entry.this;
		}
		
		public Logger.Entry writeLine (final String string){
			return this.write(string).writeLine();
		}
		
		public Logger.Entry writeLine (final Throwable throwable){
			return this.write(throwable).writeLine();
		}
	}
}