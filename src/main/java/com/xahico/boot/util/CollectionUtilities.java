/*
 * Written by Tuomas Kontiainen <https://www.github.com/tutomiko>
 * 
 * Copyright (c) 2023, Tuomas Kontiainen
 */
package com.xahico.boot.util;

import com.xahico.boot.util.transformer.ObjectTransformer;
import com.xahico.boot.util.transformer.StringObjectConstructor;
import com.xahico.boot.util.transformer.StringTransformer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class CollectionUtilities {
	public static <T> List<T> buildList (final ListBuilder<T> builder){
		return builder.build();
	}
	
	public static <T> List<T> collect (final List<T> source, final Filter<T> filter){
		final List<T> collection;
		
		collection = new ArrayList<>();
		
		for (final var element : source) {
			if (filter.accept(element)) {
				collection.add(element);
			}
		}
		
		return collection;
	}
	
	public static <T> List<T> collect (final List<T>[] sources, final Filter<T> filter){
		final List<T> collection;
		
		collection = new ArrayList<>();
		
		for (final var source : sources) {
			for (final var element : source) {
				if (filter.accept(element)) {
					collection.add(element);
				}
			}
		}
		
		return collection;
	}
	
	public static boolean containsString (final Collection<String> collection, final String string, final boolean ignoreCase){
		for (final var element : collection) {
			if (null == element) 
				continue;
			
			if ((ignoreCase && element.equalsIgnoreCase(string)) || element.equals(string)) {
				return true;
			}
		}
		
		return false;
	}
	
	public static <T extends Enum> String enumListToListString (final List<T> collection){
		final StringBuilder sb;
		
		sb = new StringBuilder();
		
		for (var i = 0; i < collection.size(); i++) {
			final T element;
			
			element = collection.get(i);
			
			sb.append(Objects.toString(element.name()));
			
			if ((i + 1) < collection.size()) {
				sb.append(',')
				  .append(' ');
			}
		}
		
		return sb.toString();
	}
	
	public static <T extends Enum> List<T> listStringToListOfEnums (final String listString, final Class<T> enumClass){
		return listStringToListOfObjects(listString, (element) -> (T) Enum.valueOf(enumClass, element));
	}
	
	public static <T> List<T> listStringToListOfObjects (final String listString, final StringObjectConstructor<T> constructor){
		final List<T> collection;
		
		collection = new ArrayList<>();
		
		StringUtilities.splitString(listString, ",", true, (element) -> collection.add(constructor.newInstance(element)));
		
		return collection;
	}
	
	public static List<String> listStringToListOfStrings (final String listString){
		final List<String> collection;
		
		collection = new ArrayList<>();
		
		StringUtilities.splitString(listString, ",", true, (element) -> collection.add(element));
		
		return collection;
	}
	
	public static List<String> listStringToListOfStrings (final String listString, final StringTransformer transformer){
		final List<String> collection;
		
		collection = new ArrayList<>();
		
		StringUtilities.splitString(listString, ",", true, (element) -> collection.add(transformer.call(element)));
		
		return collection;
	}
	
	public static <E> int removeFiltered (final Collection<E> collection, final Filter<E> filter){
		int               count;
		final Iterator<E> it;
		
		count = 0;
		
		it = collection.iterator();
		
		while (it.hasNext()) {
			final E element;
			
			element = it.next();
			
			if (filter.accept(element)) {
				it.remove();
				
				count++;
			}
		}
		
		return count;
	}
	
	public static <T> void removeMappings (final Collection<T> collection, final int... indexes){
		int               index;
		final Iterator<T> it;
		
		it = collection.iterator();
		
		index = 0;
		
		while (it.hasNext()) {
			final T element;
			
			element = it.next();
			
			if (ArrayUtilities.contains(indexes, index)) {
				it.remove();
			}
			
			index++;
		}
	}
	
	public static <T> void removeThrough (final Collection<T> collection, final int fromIndex, final int count){
		int               index;
		final Iterator<T> it;
		int               removed = 0;
		
		it = collection.iterator();
		
		index = -1;
		
		while (it.hasNext()) {
			final T element;
			
			element = it.next();
			
			index++;
			
			if (index < fromIndex) 
				continue;
			
			if (removed < count) {
				removed++;
				
				it.remove();
			} else {
				break;
			}
		}
	}
	
	public static <T> void removeThrough (final Collection<T> collection, final int count){
		removeThrough(collection, 0, count);
	}
	
	public static <T> Collection<T> reverse (final List<T> collection){
		Collections.reverse(collection);
		
		return collection;
	}
	
	public static <T> T seek (final Collection<T> collection, final Filter<T> filter, final boolean remove){
		return seek(collection, filter, remove, null);
	}
	
	public static <T> T seek (final Collection<T> collection, final Filter<T> filter, final boolean remove, final T defaultValue){
		final Iterator<T> it;
		
		it = collection.iterator();
		
		while (it.hasNext()) {
			final T element;
			
			element = it.next();
			
			if (filter.accept(element)) {
				if (remove) {
					it.remove();
				}
				
				return element;
			}
		}
		
		return defaultValue;
	}
	
	public static String stringListToListString (final List<String> collection){
		final StringBuilder sb;
		
		sb = new StringBuilder();
		
		for (var i = 0; i < collection.size(); i++) {
			final String element;
			
			element = collection.get(i);
			
			sb.append(element);
			
			if ((i + 1) < collection.size()) {
				sb.append(',')
				  .append(' ');
			}
		}
		
		return sb.toString();
	}
	
	public static String stringListToListString (final List<String> collection, final StringTransformer transformer){
		final StringBuilder sb;
		
		sb = new StringBuilder();
		
		for (var i = 0; i < collection.size(); i++) {
			final String element;
			
			element = collection.get(i);
			
			sb.append(transformer.call(element));
			
			if ((i + 1) < collection.size()) {
				sb.append(',')
				  .append(' ');
			}
		}
		
		return sb.toString();
	}
	
	public static String stringUnpack (final List<?> collection){
		final Iterator<?>   it;
		final StringBuilder sb;
		
		sb = new StringBuilder();
		
		it = collection.iterator();
		
		while (it.hasNext()) {
			final Object element;
			
			element = it.next();
			
			sb.append(element);
			
			if (it.hasNext()) {
				sb.append(", ");
			}
		}
		
		return sb.toString();
	}
	
	public static <F, T> ArrayList<T> transformObjectArrayList (final Collection<F> collection, final ObjectTransformer<F, T> transformer){
		final ArrayList<T> collectionTransform;
		
		collectionTransform = new ArrayList<>(collection.size());
		
		for (final var element : collection) {
			collectionTransform.add(transformer.call(element));
		}
		
		return collectionTransform;
	}
	
	public static <F, T> HashSet<T> transformObjectHashSet (final Collection<F> collection, final ObjectTransformer<F, T> transformer){
		final HashSet<T> collectionTransform;
		
		collectionTransform = new HashSet<>(collection.size());
		
		for (final var element : collection) {
			collectionTransform.add(transformer.call(element));
		}
		
		return collectionTransform;
	}
	
	public static <F, T> LinkedList<T> transformObjectLinkedList (final Collection<F> collection, final ObjectTransformer<F, T> transformer){
		final LinkedList<T> collectionTransform;
		
		collectionTransform = new LinkedList<>();
		
		for (final var element : collection) {
			collectionTransform.add(transformer.call(element));
		}
		
		return collectionTransform;
	}
	
	public static <T> ArrayList<T> transformStringArrayList (final Collection<String> collection, final ObjectTransformer<String, T> transformer){
		return transformObjectArrayList(collection, transformer);
	}
	
	public static <T> HashSet<T> transformStringHashSet (final Collection<String> collection, final ObjectTransformer<String, T> transformer){
		return transformObjectHashSet(collection, transformer);
	}
	
	public static <T> LinkedList<T> transformStringLinkedList (final Collection<String> collection, final ObjectTransformer<String, T> transformer){
		return transformObjectLinkedList(collection, transformer);
	}
	
	
	
	private CollectionUtilities (){
		throw new UnsupportedOperationException("Not supported.");
	}
}