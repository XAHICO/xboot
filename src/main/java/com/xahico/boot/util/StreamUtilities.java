/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutionException;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class StreamUtilities {
	public static void readAllBytesWith (final InputStream stream, final String separator, final boolean ignoreCase, final Enumerator<String> handler) throws ExecutionException, IOException {
		final StringBuilder sb;
		final int           sl;

		sl = separator.length();

		sb = new StringBuilder();

		for (;;) {
			final int  b;
			final char c;
			
			b = stream.read();

			if (b == -1) {
				break;
			}

			c = (char)(b);

			sb.append(c);

			if (sb.length() >= sl) {
				boolean lend = true;

				for (var i = 1; i <= sl; i++) {
					if (sb.charAt(sb.length() - i) != separator.charAt(sl - i)) {
						if (ignoreCase && (Character.toLowerCase(sb.charAt(sb.length() - i)) == Character.toLowerCase(separator.charAt(sl - i)))) 
							continue;
						else {
							lend = false;

							break;
						}
					}
				}

				if (lend) try {
					if (! sb.isEmpty()) try {
						handler.accept(sb.substring(0, (sb.length() - sl)));
					} catch (final Throwable t) {
						throw new ExecutionException(t);
					}
				} finally {
					sb.delete(0, sb.length());
				}
			}
		}
		
		if (! sb.isEmpty()) try {
			handler.accept(sb.toString());
		} catch (final Throwable t) {
			throw new ExecutionException(t);
		}
	}
	
	public static void readAllBytesWith (final InputStreamReader stream, final String separator, final boolean ignoreCase, final Enumerator<String> handler) throws ExecutionException, IOException {
		final StringBuilder sb;
		final int           sl;

		sl = separator.length();

		sb = new StringBuilder();

		for (;;) {
			final int  b;
			final char c;
			
			b = stream.read();

			if (b == -1) {
				break;
			}

			c = (char)(b);

			sb.append(c);

			if (sb.length() >= sl) {
				boolean lend = true;

				for (var i = 1; i <= sl; i++) {
					if (sb.charAt(sb.length() - i) != separator.charAt(sl - i)) {
						if (ignoreCase && (Character.toLowerCase(sb.charAt(sb.length() - i)) == Character.toLowerCase(separator.charAt(sl - i)))) 
							continue;
						else {
							lend = false;

							break;
						}
					}
				}

				if (lend) try {
					if (! sb.isEmpty()) try {
						handler.accept(sb.substring(0, (sb.length() - sl)));
					} catch (final Throwable t) {
						throw new ExecutionException(t);
					}
				} finally {
					sb.delete(0, sb.length());
				}
			}
		}
		
		if (! sb.isEmpty()) try {
			handler.accept(sb.toString());
		} catch (final Throwable t) {
			throw new ExecutionException(t);
		}
	}
	
	public static void readAllBytesWith (final InputStream stream, final String separator, final boolean ignoreCase, final OrderedEnumerator<String> handler) throws IOException {
		final StringBuilder sb;
		final int           sl;

		sl = separator.length();

		sb = new StringBuilder();

		for (;;) {
			final int  b;
			final char c;
			
			b = stream.read();

			if (b == -1) {
				break;
			}

			c = (char)(b);

			sb.append(c);

			if (sb.length() >= sl) {
				boolean lend = true;

				for (var i = 1; i <= sl; i++) {
					if (sb.charAt(sb.length() - i) != separator.charAt(sl - i)) {
						if (ignoreCase && (Character.toLowerCase(sb.charAt(sb.length() - i)) == Character.toLowerCase(separator.charAt(sl - i)))) 
							continue;
						else {
							lend = false;

							break;
						}
					}
				}

				if (lend) try {
					if (! sb.isEmpty()) {
						if (! handler.accept(sb.substring(0, (sb.length() - sl)))) {
							break;
						}
					}
				} finally {
					sb.delete(0, sb.length());
				}
			}
		}
		
		if (! sb.isEmpty()) {
			handler.accept(sb.toString());
		}
	}
	
	public static void readAllBytesWith (final InputStreamReader stream, final String separator, final boolean ignoreCase, final OrderedEnumerator<String> handler) throws IOException {
		final StringBuilder sb;
		final int           sl;

		sl = separator.length();

		sb = new StringBuilder();

		for (;;) {
			final int  b;
			final char c;
			
			b = stream.read();

			if (b == -1) {
				break;
			}

			c = (char)(b);

			sb.append(c);

			if (sb.length() >= sl) {
				boolean lend = true;

				for (var i = 1; i <= sl; i++) {
					if (sb.charAt(sb.length() - i) != separator.charAt(sl - i)) {
						if (ignoreCase && (Character.toLowerCase(sb.charAt(sb.length() - i)) == Character.toLowerCase(separator.charAt(sl - i)))) 
							continue;
						else {
							lend = false;

							break;
						}
					}
				}

				if (lend) try {
					if (! sb.isEmpty()) {
						if (! handler.accept(sb.substring(0, (sb.length() - sl)))) {
							break;
						}
					}
				} finally {
					sb.delete(0, sb.length());
				}
			}
		}
		
		if (! sb.isEmpty()) {
			handler.accept(sb.toString());
		}
	}
	
	public static void readNBytesWith (final InputStream stream, final int count, final String separator, final boolean ignoreCase, final Enumerator<String> handler) throws ExecutionException, IOException {
		final StringBuilder sb;
		final int           sl;

		sl = separator.length();

		sb = new StringBuilder();

		for (var cursor = 0; cursor < count; cursor++) {
			final int  b;
			final char c;
			
			b = stream.read();

			if (b == -1) {
				break;
			}

			c = (char)(b);

			sb.append(c);

			if (sb.length() >= sl) {
				boolean lend = true;

				for (var i = 1; i <= sl; i++) {
					if (sb.charAt(sb.length() - i) != separator.charAt(sl - i)) {
						if (ignoreCase && (Character.toLowerCase(sb.charAt(sb.length() - i)) == Character.toLowerCase(separator.charAt(sl - i)))) 
							continue;
						else {
							lend = false;

							break;
						}
					}
				}

				if (lend) try {
					if (! sb.isEmpty()) try {
						handler.accept(sb.substring(0, (sb.length() - sl)));
					} catch (final Throwable t) {
						throw new ExecutionException(t);
					}
				} finally {
					sb.delete(0, sb.length());
				}
			}
		}
		
		if (! sb.isEmpty()) try {
			handler.accept(sb.toString());
		} catch (final Throwable t) {
			throw new ExecutionException(t);
		}
	}
	
	public static void readNBytesWith (final InputStreamReader stream, final int count, final String separator, final boolean ignoreCase, final Enumerator<String> handler) throws ExecutionException, IOException {
		final StringBuilder sb;
		final int           sl;

		sl = separator.length();

		sb = new StringBuilder();

		for (var cursor = 0; cursor < count; cursor++) {
			final int  b;
			final char c;
			
			b = stream.read();

			if (b == -1) {
				break;
			}

			c = (char)(b);

			sb.append(c);

			if (sb.length() >= sl) {
				boolean lend = true;

				for (var i = 1; i <= sl; i++) {
					if (sb.charAt(sb.length() - i) != separator.charAt(sl - i)) {
						if (ignoreCase && (Character.toLowerCase(sb.charAt(sb.length() - i)) == Character.toLowerCase(separator.charAt(sl - i)))) 
							continue;
						else {
							lend = false;

							break;
						}
					}
				}

				if (lend) try {
					if (! sb.isEmpty()) try {
						handler.accept(sb.substring(0, (sb.length() - sl)));
					} catch (final Throwable t) {
						throw new ExecutionException(t);
					}
				} finally {
					sb.delete(0, sb.length());
				}
			}
		}
		
		if (! sb.isEmpty()) try {
			handler.accept(sb.toString());
		} catch (final Throwable t) {
			throw new ExecutionException(t);
		}
	}
	
	public static void readNBytesWith (final InputStream stream, final int count, final String separator, final boolean ignoreCase, final OrderedEnumerator<String> handler) throws IOException {
		final StringBuilder sb;
		final int           sl;

		sl = separator.length();

		sb = new StringBuilder();

		for (var cursor = 0; cursor < count; cursor++) {
			final int  b;
			final char c;
			
			b = stream.read();

			if (b == -1) {
				break;
			}

			c = (char)(b);

			sb.append(c);

			if (sb.length() >= sl) {
				boolean lend = true;

				for (var i = 1; i <= sl; i++) {
					if (sb.charAt(sb.length() - i) != separator.charAt(sl - i)) {
						if (ignoreCase && (Character.toLowerCase(sb.charAt(sb.length() - i)) == Character.toLowerCase(separator.charAt(sl - i)))) 
							continue;
						else {
							lend = false;

							break;
						}
					}
				}

				if (lend) try {
					if (! sb.isEmpty()) {
						if (! handler.accept(sb.substring(0, (sb.length() - sl)))) {
							break;
						}
					}
				} finally {
					sb.delete(0, sb.length());
				}
			}
		}
		
		if (! sb.isEmpty()) {
			handler.accept(sb.toString());
		}
	}
	
	public static void readNBytesWith (final InputStreamReader stream, final int count, final String separator, final boolean ignoreCase, final OrderedEnumerator<String> handler) throws IOException {
		final StringBuilder sb;
		final int           sl;

		sl = separator.length();

		sb = new StringBuilder();

		for (var cursor = 0; cursor < count; cursor++) {
			final int  b;
			final char c;
			
			b = stream.read();

			if (b == -1) {
				break;
			}

			c = (char)(b);

			sb.append(c);

			if (sb.length() >= sl) {
				boolean lend = true;

				for (var i = 1; i <= sl; i++) {
					if (sb.charAt(sb.length() - i) != separator.charAt(sl - i)) {
						if (ignoreCase && (Character.toLowerCase(sb.charAt(sb.length() - i)) == Character.toLowerCase(separator.charAt(sl - i)))) 
							continue;
						else {
							lend = false;

							break;
						}
					}
				}

				if (lend) try {
					if (! sb.isEmpty()) {
						if (! handler.accept(sb.substring(0, (sb.length() - sl)))) {
							break;
						}
					}
				} finally {
					sb.delete(0, sb.length());
				}
			}
		}
		
		if (! sb.isEmpty()) {
			handler.accept(sb.toString());
		}
	}
	
	
	
	private StreamUtilities (){
		throw new UnsupportedOperationException("Not supported.");
	}
}