/*
 * Written by Tuomas Kontiainen <https://www.github.com/tutomiko>
 * 
 * Copyright (c) 2023, Tuomas Kontiainen
 */
package com.xahico.boot.cryptography;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class SSLUtilities {
	public static SSLContext loadContextFile (final File keyFile, final String password){
		try {
			final SSLContext          ctx;
			final KeyManagerFactory   kmf;
			final KeyStore            ks;
			final TrustManagerFactory tmf;
			
			try (final var stream = new FileInputStream(keyFile)) {
				ks = KeyStore.getInstance("JKS");
				ks.load(stream, password.toCharArray());
				
			} catch (final FileNotFoundException | KeyStoreException ex) {
				throw new Error(ex);
			} catch (final IOException ex) {
				throw new InternalError(ex);
			}
			
			try {
				kmf = KeyManagerFactory.getInstance("SunX509");
				kmf.init(ks, password.toCharArray());
			} catch (final KeyStoreException | UnrecoverableKeyException ex) {
				throw new Error(ex);
			}
			
			try {
				tmf = TrustManagerFactory.getInstance("SunX509");
				tmf.init(ks);
			} catch (final KeyStoreException ex) {
				throw new Error(ex);
			}
			
			try {
				ctx = SSLContext.getInstance("TLS");
				ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
			} catch (final KeyManagementException ex) {
				throw new Error(ex);
			}
			
			return ctx;
		} catch (final CertificateException | NoSuchAlgorithmException ex) {
			throw new InternalError(ex);
		}
	}
	
	
	
	private SSLUtilities (){
		throw new UnsupportedOperationException("Not supported.");
	}
}