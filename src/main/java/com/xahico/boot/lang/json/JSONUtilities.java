/*
 * Written by Tuomas Kontiainen <https://www.github.com/tutomiko>
 * 
 * Copyright (c) 2023, Tuomas Kontiainen
 */
package com.xahico.boot.lang.json;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class JSONUtilities {
	private static final Pattern PATTERN_ARRAY_UNSTRUCTURED = Pattern.compile("(\\S+)\\[([0-9]*\\??)]??\\]$");
	
	
	
	public static JSONObject compileUnstructuredArrays (final JSONObject json){
		final Iterator<String> it;
		final Deque<Runnable>  stack;
		
		stack = new LinkedList<>();
		
		for (final var key : json.keySet()) {
			final String  arrayName;
			final Matcher matcher;
			
			if (null == key) 
				continue;
			
			matcher = PATTERN_ARRAY_UNSTRUCTURED.matcher(key);
			
			if (! matcher.matches()) {
				continue;
			}
			
			arrayName = matcher.group(1);
			
			if (! json.has(arrayName)) {
				stack.add(() -> json.put(arrayName, new JSONArray()));
			}
		}
		
		if (! stack.isEmpty()) try {
			stack.forEach(call -> call.run());
		} finally {
			stack.clear();
		}
		
		it = json.keys();
		
		while (it.hasNext()) {
			final JSONArray array;
			final Object    arrayElement;
			final int       arrayElementIndex;
			final String    arrayElementIndexAssign;
			final boolean   arrayElementIndexOptional;
			final String    arrayName;
			final String    key;
			final Matcher   matcher;
			
			key = it.next();
			
			if (null == key) 
				continue;
			
			matcher = PATTERN_ARRAY_UNSTRUCTURED.matcher(key);
			
			if (! matcher.matches()) {
				continue;
			}
			
			arrayName = matcher.group(1);
			arrayElementIndexAssign = (matcher.group(2).isBlank() ? "-1" : matcher.group(2));
			arrayElementIndexOptional = arrayElementIndexAssign.endsWith("?");
			arrayElementIndex = Integer.parseInt(arrayElementIndexOptional ? arrayElementIndexAssign.substring(0, (arrayElementIndexAssign.length() - 1)) : arrayElementIndexAssign);
			array = json.getJSONArray(arrayName);
			arrayElement = json.get(key);
			
			if (arrayElementIndex == -1) {
				stack.addLast(() -> array.put(arrayElement));
			} else {
				if (arrayElementIndexOptional && (array.length() < arrayElementIndex)) {
					stack.addLast(() -> array.put(arrayElement));
				} else {
					stack.addFirst(() -> {
						if (arrayElementIndexOptional) 
							array.put(arrayElement);
						else {
							array.put(arrayElementIndex, arrayElement);
						}
					});
				}
			}
			
			it.remove();
		}
		
		if (! stack.isEmpty()) try {
			stack.forEach(call -> call.run());
		} finally {
			stack.clear();
		}
		
		return json;
	}
	
	public static boolean containsUnstructuredArrays (final JSONObject json){
		for (final var key : json.keySet()) {
			if (null == key) 
				continue;
			
			if (PATTERN_ARRAY_UNSTRUCTURED.matcher(key).matches()) {
				return true;
			}
		}
		
		return false;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	@Deprecated
	public static String listToString (final List<? extends JSONSerializable> list){
		final String         arrayString;
		final Base64.Encoder encoder;
		final StringBuilder  sb;
		
		encoder = Base64.getMimeEncoder();
		
		sb = new StringBuilder();
		
		for (var i = 0; i < list.size(); i++) {
			final JSONSerializable element;
			final JSONObject       elementJSON;
			final String           elementString;
			final String           elementStringEncoded;
			
			element = list.get(i);
			elementJSON = element.json();
			elementString = elementJSON.toString();
			elementStringEncoded = encoder.encodeToString(elementString.getBytes());
			
			sb.append(elementStringEncoded);
			
			if ((i + 1) < list.size()) {
				sb.append(',')
				  .append(' ');
			}
		}
		
		arrayString = encoder.encodeToString(sb.toString().getBytes());
		
		return arrayString;
	}
	
	@Deprecated
	public static <T extends JSONSerializable> List<T> stringToList (final String arrayString, final Class<T> jclass){
		final JSONFactory<T> factory;
		
		factory = JSONFactory.getJSONFactory(jclass);
		
		return stringToList(arrayString, (json) -> factory.newInstance(json));
	}
	
	@Deprecated
	public static <T extends JSONSerializable> List<T> stringToList (final String arrayString, final Class<T> jclass, final List<T> collection){
		final JSONFactory<T> factory;
		
		factory = JSONFactory.getJSONFactory(jclass);
		
		return stringToList(arrayString, (json) -> factory.newInstance(json), collection);
	}
	
	@Deprecated
	public static <T extends JSONSerializable> List<T> stringToList (final String arrayString, final JSONConstructor<T> constructor){
		final List<T> collection;
		
		collection = new ArrayList<>();
		
		return stringToList(arrayString, constructor, collection);
	}
	
	@Deprecated
	public static <T extends JSONSerializable> List<T> stringToList (final String arrayString, final JSONConstructor<T> constructor, final List<T> collection){
		final String         arrayStringDecoded;
		final Base64.Decoder decoder;
		
		decoder = Base64.getMimeDecoder();
		
		if (arrayString.isBlank() || arrayString.isEmpty()) 
			return collection;
		
		arrayStringDecoded = new String(decoder.decode(arrayString.getBytes()));
		
		for (final String elementStringEncoded : arrayStringDecoded.split("\\s*,\\s*")) {
			final T          element;
			final JSONObject elementJSON;
			final String     elementString;
			
			elementString = new String(decoder.decode(elementStringEncoded.getBytes()));
			elementJSON = new JSONObject(elementString);
			element = constructor.newInstance(elementJSON);
			
			collection.add(element);
		}
		
		return collection;
	}
	
	
	
	private JSONUtilities (){
		throw new UnsupportedOperationException("Not supported.");
	}
}