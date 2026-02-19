/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.net.sock.model.fts;

import java.util.Arrays;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class FTSUtilities {
	public static byte[] addPadding (byte[] array){
		final int length;
		final int lengthPadded;
		
		length = array.length;
		lengthPadded = (int) calculatePadding(array);
		
		if (lengthPadded != length) {
			final int padding;
			
			padding = (lengthPadded - length);
			
			array = Arrays.copyOf(array, lengthPadded);
			
			for (var i = 0; i < padding; i++) {
				array[length + i] = FTSCryptor.ALGORITHM_PAD;
			}
		}
		
		return array;
	}
	
	public static long calculatePadding (final byte[] array){
		return calculatePadding(array.length);
	}
	
	public static long calculatePadding (final long length){
		if ((length % FTSCryptor.ALGORITHM_BLOCK_LENGTH) != 0) {
			return (length + (FTSCryptor.ALGORITHM_BLOCK_LENGTH - (length % FTSCryptor.ALGORITHM_BLOCK_LENGTH)));
		} else {
			return length;
		}
	}
	
	public static byte[] removePadding (final byte[] array){
		int length;
		
		length = array.length;
		
		for (var i = 0; i < array.length; i++) {
			if (array[i] == FTSCryptor.ALGORITHM_PAD) {
				length = i;
				
				break;
			}
		}
		
		return Arrays.copyOfRange(array, 0, length);
	}
	
	
	
	private FTSUtilities (){
		throw new UnsupportedOperationException("Not supported.");
	}
}