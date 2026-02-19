/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.net.sock.model.fts;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
final class FTSSecurityProvider {
	public static final String  ALGORITHM = "RSA/ECB/PKCS1Padding";
	public static final int     ALGORITHM_BITS = 2048;
	public static final int     PACKET_SIZE = (ALGORITHM_BITS / 8);
	
	
	
	public static KeyPair generateKeyPair (){
		try {
			final KeyPairGenerator keygen;
			
			keygen = KeyPairGenerator.getInstance("RSA");
			keygen.initialize(ALGORITHM_BITS, new SecureRandom());
			
			return keygen.generateKeyPair();
		} catch (final NoSuchAlgorithmException ex) {
			throw new Error(ex);
		}
	}
	
	
	
	private final Cipher  cipher_decrypt;
	private final Cipher  cipher_encrypt;
	private final KeyPair keypair;
	
	
	
	public FTSSecurityProvider (){
		super();
		
		try {
			this.keypair = generateKeyPair();
			
			this.cipher_encrypt = Cipher.getInstance(ALGORITHM);
			this.cipher_encrypt.init(Cipher.ENCRYPT_MODE, this.keypair.getPublic());
			
			this.cipher_decrypt = Cipher.getInstance(ALGORITHM);
			this.cipher_decrypt.init(Cipher.DECRYPT_MODE, this.keypair.getPrivate());
		} catch (final InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException ex) {
			throw new InternalError(ex);
		}
	}
	
	
	
	public synchronized byte[] decrypt (final byte[] data){
		try {
			return cipher_decrypt.doFinal(data);
		} catch (final BadPaddingException | IllegalBlockSizeException ex) {
			return null;
		}
	}
	
	public synchronized byte[] encrypt (final byte[] data){
		try {
			return cipher_encrypt.doFinal(data);
		} catch (final BadPaddingException | IllegalBlockSizeException ex) {
			return null;
		}
	}
	
	public KeyPair getKeyPair (){
		return keypair;
	}
	
	public byte[] transferable (){
		final StringWriter stringWrapper;
		
		stringWrapper = new StringWriter();
		
		try (final var writer = new JcaPEMWriter(stringWrapper)) {
			writer.writeObject(this.keypair.getPublic());
		} catch (final IOException ex) {
			throw new Error(ex);
		}
		
		return stringWrapper.getBuffer().toString().getBytes(StandardCharsets.UTF_8);
	}
}