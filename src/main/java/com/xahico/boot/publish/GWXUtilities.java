/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.publish;

import com.xahico.boot.lang.jsox.JSOXUtilities;
import com.xahico.boot.lang.jsox.JSOXVariant;
import com.xahico.boot.platform.FileUtilities;
import io.netty.handler.codec.http.QueryStringDecoder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import javax.net.ssl.KeyManagerFactory;
import org.python.core.Py;
import org.python.core.PyException;
import org.python.core.PyObject;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class GWXUtilities {
	public static String buildTokenIdentity (final Class<?> jclass){
		final String        className;
		final StringBuilder sb;
		
		className = jclass.getSimpleName();

		sb = new StringBuilder();

		for (var i = 0; i < className.length(); i++) {
			final char c;

			c = className.charAt(i);

			if (Character.isLowerCase(c)) {
				sb.append(c);
			} else {
				if ((i > 0) && ((i + 1) < className.length()) && (!Character.isUpperCase(className.charAt(i - 1)) || Character.isLowerCase(className.charAt(i + 1)))) {
					sb.append('-');
				}

				sb.append(Character.toLowerCase(c));
			}
		}
		
		sb.append("-");
		sb.append("token");

		return sb.toString();
	}
	
	public static boolean checkNodeKey (final GWXObject node, final Object key){
		return node.checkKey(key);
	}
	
	public static GWXSupportedMimeType detectMimeType (final File file, final GWXSupportedMimeType defaultTo) throws IOException {
		return getMimeTypeForExtension(FileUtilities.getFileExtension(file), defaultTo);
	}
	
	public static GWXSupportedMimeType detectMimeType (final Path filePath, final GWXSupportedMimeType defaultTo) throws IOException {
		return getMimeTypeForExtension(FileUtilities.getFileExtension(filePath), defaultTo);
	}
	
	public static String formatMimeType (final GWXSupportedMimeType type, final Charset charset){
		return "%s; charset=%s".formatted(type.toString(), translateCharsetToString(charset));
	}
	
	public static GWXSupportedMimeType getMimeTypeForExtension (final String extension, final GWXSupportedMimeType defaultTo){
		return GWXSupportedMimeType.getMimeTypeForExtension(extension, defaultTo);
	}
	
	public static Object getNodeId (final GWXObject object){
		return object.getId();
	}
	
	public static GWXObject getObjectParent (final GWXObject object){
		return object.getParent();
	}
	
	public static KeyManagerFactory loadKeyManagerPKCS12 (final File file, final char[] password) throws CertificateException, FileNotFoundException {
		try {
			final KeyManagerFactory kmf;
			final KeyStore          ks;
			
			ks = KeyStore.getInstance("PKCS12");
			
			try (final var in = new FileInputStream(file)) {
				ks.load(in, password);
			}
			
			kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			kmf.init(ks, password);
			
			return kmf;
		} catch (final IOException | KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException ex) {
			throw new Error(ex);
		}
	}
	
	public static KeyManagerFactory loadKeyManagerPKCS12 (final File file, final String password) throws CertificateException, FileNotFoundException {
		return loadKeyManagerPKCS12(file, password.toCharArray());
	}
	
	public static JSOXVariant parseQueryString (final String uri){
		return JSOXUtilities.transform(new QueryStringDecoder(uri).parameters());
	}
	
	public static String path (final GWXObject object){
		return object.path();
	}
	
	public static String translateCharsetToString (final Charset charset){
		if (charset == StandardCharsets.UTF_8) 
			return "utf-8";
		else if (charset == StandardCharsets.UTF_16) 
			return "utf-16";
		else {
			throw new Error();
		}
	}
	
	static GWXException translateRCSException (final PyException ex){
		final Object cause;
		
		if (ex.match(Py.ValueError)) {
			return new GWXCustomException(GWXStatus.UNKNOWN, ex.fillInStackTrace());
		}
		
		if (ex.value instanceof PyObject) {
			cause = ex.value.__tojava__(Throwable.class);
		} else {
			cause = ex.getCause();
		}

		if (cause instanceof GWXException) {
			return (GWXException)(cause);
		} else {
			return new GWXCustomException(GWXStatus.UNKNOWN, ex);
		}
	}
	
	
	
	private GWXUtilities (){
		throw new UnsupportedOperationException("Not supported.");
	}
}