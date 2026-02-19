/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.algorithm;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class Generators {
	/**
	 * Generates an array of length {@code length} 
	 * with values distributed reflectively as mirrored pairs along the 
	 * midsection of the array, with a guarantee of layout justification 
	 * through entanglement via reflection 
	 * where a "shadow" reflects, numerically, its cast {@code -1}.
	 * 
	 * @param length 
	 * Length of array
	 * 
	 * @return 
	 * Generated array
	**/
	public static int[] generateEH0 (final int length){
		final int[] array;
		int         cursor_l;
		int         cursor_r;
		final int   delimiter;
		
		array = new int[length];
		
		if ((length % 2) == 0) {
			delimiter = ((length / 2) - 1);
			cursor_l = 0;
			cursor_r = 1;
		} else {
			delimiter = (length / 2);
			cursor_l = 1;
			cursor_r = 0;
		}
		
		for (var i = 0; i < length; i++) {
			if ((((length % 2) == 0) && ((i % 2) == 0)) || (((length % 2) != 0) && ((i % 2) != 0))) {
				array[delimiter - cursor_l] = i;

				cursor_l++;
			} else {
				array[delimiter + cursor_r] = i;

				cursor_r++;
			}
		}
		
		return array;
	}
	
	/**
	 * Generates an array of length {@code length} 
	 * with values distributed similarly to the 
	 * {@link #generateEH0(int) EH0 generator algorithm} 
	 * albeit in randomized layout and without any guarantees of 
	 * layout justification
	 * 
	 * @param length 
	 * Length of array
	 * 
	 * @return 
	 * Generated array
	**/
	public static int[] generateEH0R (final int length){
		final int[]         array;
		final List<Integer> buffer;
		final Random        random;
		int                 select;
		
		random = new Random(length * System.currentTimeMillis());
		
		buffer = new ArrayList<>(length);
		
		for (var i = 0; i < length; i++) {
			select = random.nextInt(2);
			
			if (select == 1) {
				buffer.add(buffer.size(), i);
			} else {
				buffer.add(0, i);
			}
		}
		
		array = new int[length];
		
		for (var i = 0; i < buffer.size(); i++) {
			array[i] = buffer.get(i);
		}
		
		return array;
	}
	
	/**
	 * Generates an array of length {@code length} 
	 * with values distributed reflectively along the midsection, 
	 * with the reflective "false" half (left) copying the "true" half (right)
	 * 
	 * @param length 
	 * Length of array
	 * 
	 * @param negativeReflection 
	 * {@code true} to reflect values negatively, 
	 * {@code false} to reflect values identically
	 * 
	 * @return 
	 * Generated array
	**/
	public static int[] generateMRC1 (final int length, final boolean negativeReflection){
		final int[] array;
		
		array = new int[(length * 2) - 1];
		
		for (var i = 1; i < length; i++) {
			array[length - 1 + i] = i;
			array[length - 1 - i] = (negativeReflection ? -i : i);
		}
		
		return array;
	}
	
	
	
	private Generators (){
		throw new UnsupportedOperationException("Not supported.");
	}
}