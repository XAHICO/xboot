/*
 * Written by Tuomas Kontiainen <https://www.github.com/tutomiko>
 * 
 * Copyright (c) 2023, Tuomas Kontiainen
 */
package com.xahico.boot.util;

import com.xahico.boot.util.transformer.ObjectTransformer;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class ArrayUtilities {
	public static String arrayToReadableString (final Object[] array){
		final StringBuilder sb;
		
		sb = new StringBuilder();
		sb.append('[');
		
		for (var i = 0; i < array.length; i++) {
			final Object element;
			
			element = array[i];
			
			sb.append(element);
			
			if ((i + 1) < array.length) {
				sb.append(',');
				sb.append(' ');
			}
		}
		
		sb.append(']');
		
		return sb.toString();
	}
	
	public static String arrayToString (final Object[] array){
		final StringBuilder sb;
		
		sb = new StringBuilder();
		
		for (var i = 0; i < array.length; i++) {
			final String element;
			
			element = Objects.toString(array[i]);
			
			sb.append(element);
			
			if ((i + 1) < array.length) {
				sb.append(',')
				  .append(' ');
			}
		}
		
		return sb.toString();
	}
	
	public static <T> boolean contains (final int[] array, final int element){
		for (final var ival : array) {
			if (ival == element) {
				return true;
			}
		}
		
		return false;
	}
	
	public static <T> boolean contains (final T[] array, final T element){
		for (final var obj : array) {
			if (Objects.equals(obj, element)) {
				return true;
			}
		}
		
		return false;
	}
	
	public static boolean containsString (final Object[] array, final String s){
		for (final var aobj : array) {
			if (aobj instanceof String && s.equals(aobj)) {
				return true;
			}
		}
		
		return false;
	}
	
	public static boolean containsStringIgnoreCase (final Object[] array, final String s){
		for (final var aobj : array) {
			if (aobj instanceof String && s.equalsIgnoreCase((String)aobj)) {
				return true;
			}
		}
		
		return false;
	}
	
	public static <T extends Enum> String enumArrayToString (final T[] array){
		final StringBuilder sb;
		
		sb = new StringBuilder();
		
		for (var i = 0; i < array.length; i++) {
			final String element;
			
			element = array[i].name();
			
			sb.append(Objects.toString(element));
			
			if ((i + 1) < array.length) {
				sb.append(',')
				  .append(' ');
			}
		}
		
		return sb.toString();
	}
	
	public static <T extends Enum> T[] enumStringToArray (final String arrayString, final Class<T> enumClass){
		final T[]     array;
		final List<T> collection;
		
		if (arrayString.isEmpty() || arrayString.isBlank()) {
			array = (T[]) Array.newInstance(enumClass, 0);
			
			return array;
		} else {
			collection = new ArrayList<>();
			
			for (final String name : arrayString.split("\\s*,\\s*")) {
				final T enumItem;

				enumItem = (T) Enum.valueOf(enumClass, name);

				collection.add(enumItem);
			}

			array = (T[]) Array.newInstance(enumClass, collection.size());
			
			return collection.toArray(array);
		}
	}
	
	public static <T> int indexOf (final T[] array, final T element){
		for (var i = 0; i < array.length; i++) {
			final T arrayElement;
			
			arrayElement = array[i];
			
			if (Objects.equals(arrayElement, element)) {
				return i;
			}
		}
		
		return -1;
	}
	
	public static <T> T[] merge (final Class<T> componentType, final T[]... arrays){
		final T[] array;
		int       arrayLength;
		int       cursor;
		
		arrayLength = 0;
		
		for (final var arrayx : arrays) {
			arrayLength += arrayx.length;
		}
		
		array = (T[]) Array.newInstance(componentType, arrayLength);
		
		cursor = 0;
		
		for (final var arrayx : arrays) {
			for (var i = 0; i < arrayx.length; i++) {
				array[cursor] = arrayx[i];
				
				cursor++;
			}
		}
		
		return array;
	}
	
	public static <T> T[] merge (final Class<T> componentType, final T[] array1, final T[] array2){
		final T[] array;
		final int arrayLength;
		int       cursor;
		
		arrayLength = (array1.length + array2.length);
		
		array = (T[]) Array.newInstance(componentType, arrayLength);
		
		cursor = 0;
		
		for (var i = 0; i < array1.length; i++) {
			array[cursor] = array1[i];
			
			cursor++;
		}
		
		for (var i = 0; i < array2.length; i++) {
			array[cursor] = array2[i];
			
			cursor++;
		}
		
		return array;
	}
	
	public static <T> T[] parseReadableString (final String string, final Class<T> typeClass, final ObjectTransformer<String, T> transformer){
		final T[]      array;
		final int      count;
		final String[] parts;
		
		if (!string.startsWith("[") || !string.endsWith("]")) {
			return null;
		}
		
		parts = StringUtilities.splitString(string.substring(1, (string.length() - 1)), ",", true);
		
		count = parts.length;
		
		array = (T[]) Array.newInstance(typeClass, count);
		
		for (var i = 0; i < count; i++) {
			array[i] = transformer.call(parts[i]);
		}
		
		return array;
	}
	
	public static void printIntegerArray (final int[] array){
		System.out.print("[");
		
		for (var i = 0; i < array.length; i++) {
			System.out.print(array[i]);
			
			if ((i + 1) < array.length) {
				System.out.print(", ");
			}
		}
		
		System.out.println("]");
	}
	
	public static <T> void printObjectArray (final T[] array){
		System.out.print("[");
		
		for (var i = 0; i < array.length; i++) {
			System.out.print(array[i]);
			
			if ((i + 1) < array.length) {
				System.out.print(", ");
			}
		}
		
		System.out.println("]");
	}
	
	public static <T> T[] putAll (final T[] array, final int offset, final T... elements){
		for (var i = 0; i < elements.length; i++) {
			final T   element;
			final int position;
			
			element = elements[i];
			
			position = (offset + i);
			
			array[position] = element;
		}
		
		return array;
	}
	
	public static String[] stringToArray (final String arrayString){
		final List<String> collection;
		
		collection = new ArrayList<>();
		
		for (final String element : arrayString.split(",\\s*")) {
			collection.add(element);
		}
		
		return collection.toArray(new String[collection.size()]);
	}
	
	public static Boolean[] transformObjectArrayToBooleanArray (final Object[] array){
		final Boolean[] newArray;
		
		newArray = new Boolean[array.length];
		
		for (var i = 0; i < array.length; i++) {
			newArray[i] = (Boolean) array[i];
		}
		
		return newArray;
	}
	
	public static boolean[] transformObjectArrayToBooleanNativeArray (final Object[] array){
		final boolean[] newArray;
		
		newArray = new boolean[array.length];
		
		for (var i = 0; i < array.length; i++) {
			newArray[i] = (boolean) array[i];
		}
		
		return newArray;
	}
	
	public static Byte[] transformObjectArrayToByteArray (final Object[] array){
		final Byte[] newArray;
		
		newArray = new Byte[array.length];
		
		for (var i = 0; i < array.length; i++) {
			newArray[i] = (Byte) array[i];
		}
		
		return newArray;
	}
	
	public static byte[] transformObjectArrayToByteNativeArray (final Object[] array){
		final byte[] newArray;
		
		newArray = new byte[array.length];
		
		for (var i = 0; i < array.length; i++) {
			newArray[i] = (byte) array[i];
		}
		
		return newArray;
	}
	
	public static Character[] transformObjectArrayToCharArray (final Object[] array){
		final Character[] newArray;
		
		newArray = new Character[array.length];
		
		for (var i = 0; i < array.length; i++) {
			newArray[i] = (Character) array[i];
		}
		
		return newArray;
	}
	
	public static char[] transformObjectArrayToCharNativeArray (final Object[] array){
		final char[] newArray;
		
		newArray = new char[array.length];
		
		for (var i = 0; i < array.length; i++) {
			newArray[i] = (char) array[i];
		}
		
		return newArray;
	}
	
	public static Double[] transformObjectArrayToDoubleArray (final Object[] array){
		final Double[] newArray;
		
		newArray = new Double[array.length];
		
		for (var i = 0; i < array.length; i++) {
			newArray[i] = (Double) array[i];
		}
		
		return newArray;
	}
	
	public static double[] transformObjectArrayToDoubleNativeArray (final Object[] array){
		final double[] newArray;
		
		newArray = new double[array.length];
		
		for (var i = 0; i < array.length; i++) {
			newArray[i] = (double) array[i];
		}
		
		return newArray;
	}
	
	public static Float[] transformObjectArrayToFloatArray (final Object[] array){
		final Float[] newArray;
		
		newArray = new Float[array.length];
		
		for (var i = 0; i < array.length; i++) {
			newArray[i] = (Float) array[i];
		}
		
		return newArray;
	}
	
	public static float[] transformObjectArrayToFloatNativeArray (final Object[] array){
		final float[] newArray;
		
		newArray = new float[array.length];
		
		for (var i = 0; i < array.length; i++) {
			newArray[i] = (float) array[i];
		}
		
		return newArray;
	}
	
	public static Integer[] transformObjectArrayToIntegerArray (final Object[] array){
		final Integer[] newArray;
		
		newArray = new Integer[array.length];
		
		for (var i = 0; i < array.length; i++) {
			newArray[i] = (Integer) array[i];
		}
		
		return newArray;
	}
	
	public static int[] transformObjectArrayToIntegerNativeArray (final Object[] array){
		final int[] newArray;
		
		newArray = new int[array.length];
		
		for (var i = 0; i < array.length; i++) {
			newArray[i] = (int) array[i];
		}
		
		return newArray;
	}
	
	public static Long[] transformObjectArrayToLongArray (final Object[] array){
		final Long[] newArray;
		
		newArray = new Long[array.length];
		
		for (var i = 0; i < array.length; i++) {
			newArray[i] = (Long) array[i];
		}
		
		return newArray;
	}
	
	public static long[] transformObjectArrayToLongNativeArray (final Object[] array){
		final long[] newArray;
		
		newArray = new long[array.length];
		
		for (var i = 0; i < array.length; i++) {
			newArray[i] = (long) array[i];
		}
		
		return newArray;
	}
	
	public static Short[] transformObjectArrayToShortArray (final Object[] array){
		final Short[] newArray;
		
		newArray = new Short[array.length];
		
		for (var i = 0; i < array.length; i++) {
			newArray[i] = (Short) array[i];
		}
		
		return newArray;
	}
	
	public static short[] transformObjectArrayToShortNativeArray (final Object[] array){
		final short[] newArray;
		
		newArray = new short[array.length];
		
		for (var i = 0; i < array.length; i++) {
			newArray[i] = (short) array[i];
		}
		
		return newArray;
	}
	
	public static String[] transformObjectArrayToStringArray (final Object[] array){
		final String[] newArray;
		
		newArray = new String[array.length];
		
		for (var i = 0; i < array.length; i++) {
			newArray[i] = Objects.toString(array[i]);
		}
		
		return newArray;
	}
	
	
	
	private ArrayUtilities (){
		throw new UnsupportedOperationException("Not supported.");
	}
}