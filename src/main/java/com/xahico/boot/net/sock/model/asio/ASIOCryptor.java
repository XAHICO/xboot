/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.net.sock.model.asio;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
final class ASIOCryptor {
	public static final String ALGORITHM = "AES/CBC/NoPadding";
	public static final int    ALGORITHM_BITS = 256;
	public static final int    ALGORITHM_BLOCK_LENGTH = (ALGORITHM_BITS / 16);
	public static final int    ALGORITHM_IV_LENGTH = (ALGORITHM_BITS / 16);
	public static final int    ALGORITHM_KEY_LENGTH = (ALGORITHM_BITS / 8);
	public static final char   ALGORITHM_PAD = 0x0;
	
	
	
	private static byte[] pad (final byte[] bytes){
		final byte[] bytesNew;
		final int    padding;
		
		padding = (16 - (bytes.length % 16));
		
		bytesNew = new byte[bytes.length + padding];
		
		System.arraycopy(bytes, 0, bytesNew, 0, bytes.length);
		
		return bytesNew;
	}
	
	private static boolean requiresPadding (final byte[] bytes){
		return ((bytes.length % 16) != 0);
	}
	
	
	
	private final Cipher    cipher;
	private IvParameterSpec iv;
	private Key             key;
	
	
	
	public ASIOCryptor (){
		try {
			this.cipher = Cipher.getInstance(ALGORITHM);
		} catch (final NoSuchAlgorithmException | NoSuchPaddingException ex) {
			throw new RuntimeException(ex);
		}
	}
	
	
	
	public byte[] decrypt (final byte[] ciphertext){
		try {
			final IvParameterSpec ivCopy;
			final byte[]          plaintext;
			
			ivCopy = new IvParameterSpec(iv.getIV());
			
			this.cipher.init(Cipher.DECRYPT_MODE, this.key, ivCopy);
			
			plaintext = this.cipher.doFinal(ciphertext);
			
			return plaintext;
		} catch (final InvalidKeyException ex) {
			throw new Error(ex);
		} catch (IllegalBlockSizeException | BadPaddingException ex) {
			throw new Error(ex);
		} catch (InvalidAlgorithmParameterException ex) {
			throw new Error(ex);
		}
	}
	
	public byte[] encrypt (final byte[] plaintext){
		try {
			final byte[] ciphertext;
			
			if (requiresPadding(plaintext)) {
				return this.encrypt(pad(plaintext));
			}
			
			this.cipher.init(Cipher.ENCRYPT_MODE, this.key, this.iv);
			
			ciphertext = this.cipher.doFinal(plaintext);
			
			return ciphertext;
		} catch (final InvalidKeyException ex) {
			throw new Error(ex);
		} catch (IllegalBlockSizeException | BadPaddingException ex) {
			throw new Error(ex);
		} catch (InvalidAlgorithmParameterException ex) {
			throw new Error(ex);
		}
	}
	
	public void init (final byte[] iv, final byte[] key){
		this.iv = new IvParameterSpec(iv);
		this.key = new SecretKeySpec(key, "AES");
	}
}