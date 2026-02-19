/*
 * Written by Tuomas Kontiainen <https://www.github.com/tutomiko>
 * 
 * Copyright (c) 2023, Tuomas Kontiainen
 */
package com.xahico.boot.lang.jsox;

import com.xahico.boot.io.IOByteBuffer;
import com.xahico.boot.reflection.ReflectionType;
import com.xahico.boot.util.BooleanUtilities;
import com.xahico.boot.util.Exceptions;
import com.xahico.boot.util.StringUtilities;
import java.lang.reflect.Array;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class JSOXUtilities {
	public static JSOXArray arrayToJSON (final Object[] array) throws JSOXInvalidTypeException {
		return arrayToJSON(array, ReflectionType.genericType(array.getClass().getComponentType()));
	}
	
	public static JSOXArray arrayToJSON (final Object[] array, final ReflectionType type) throws JSOXInvalidTypeException {
		final JSOXArray wrapper;
		
		wrapper = new JSOXArray();
		
		for (final var element : array) {
			wrapper.add(castToJSON(element, type));
		}
		
		return wrapper;
	}
	
	public static Object castFromString (final String obj, final ReflectionType type) throws JSOXInvalidTypeException {
		final Class<?> typeClass;
		
		if ((null == obj) || obj.equals("null")) {
			return null;
		}
		
		typeClass = type.getTypeClass();
		
		if (typeClass == String.class) {
			return obj;
		}
		
		if (typeClass.isEnum()) {
			return parseEnumString((Class<Enum>) typeClass, obj);
		}
		
		if (typeClass.isArray()) {
			return parseArrayString(obj, type.getComponentType());
		}
		
		if (List.class.isAssignableFrom(typeClass)) {
			return parseListString(obj, type.getGenericTypeArguments()[0]);
		}
		
		if (JSOXVariant.class.isAssignableFrom(typeClass)) {
			return new JSOXVariant(obj);
		}
		
		if (JSOXObject.class.isAssignableFrom(typeClass)) {
			return JSOXObject.newInstanceOf((Class<? extends JSOXObject>) typeClass, new JSONObject(obj));
		}
		
		if (Map.class.isAssignableFrom(typeClass)) {
			return parseMapString(obj, type.getGenericTypeArguments()[0], type.getGenericTypeArguments()[1]);
		}
		
		if ((typeClass == boolean.class) || (typeClass == Boolean.class)) {
			return Boolean.parseBoolean(obj);
		}
		
		if ((typeClass == byte.class) || (typeClass == Byte.class)) {
			return Byte.parseByte(obj);
		}
		
		if ((typeClass == char.class) || (typeClass == Character.class)) {
			if (obj.isEmpty()) 
				return "";
			else {
				return obj.charAt(0);
			}
		}
		
		if ((typeClass == double.class) || (typeClass == Double.class)) {
			return Double.parseDouble(obj);
		}
		
		if ((typeClass == int.class) || (typeClass == Integer.class)) {
			return Integer.parseInt(obj);
		}
		
		if ((typeClass == long.class) || (typeClass == Long.class)) {
			return Long.parseLong(obj);
		}
		
		if ((typeClass == short.class) || (typeClass == Short.class)) {
			return Short.parseShort(obj);
		}
		
		throw new JSOXInvalidTypeException("Unable to cast '%s' to %s".formatted(obj, type));
	}
	
	public static Object castToJSON (final Object obj, final ReflectionType type) throws JSOXInvalidTypeException {
		final Class<?> typeClass;
		
		if (null == obj) {
			return null;
		}
		
		typeClass = type.getTypeClass();
		
		if (typeClass == String.class) {
			return Objects.toString(obj);
		}
		
		if (typeClass.isEnum()) {
			return enumToString((Enum<?>) obj);
		}
		
		if (typeClass.isArray()) {
			return arrayToJSON((Object[]) obj, type.getComponentType());
		}
		
		if (List.class.isAssignableFrom(typeClass)) {
			return listToJSON((List<?>) obj, type.getGenericTypeArguments()[0]);
		}
		
		if (JSOXVariant.class.isAssignableFrom(typeClass)) {
			return ((JSOXVariant) obj).json();
		}
		
		if (JSOXObject.class.isAssignableFrom(typeClass)) {
			return ((JSOXObject) obj).json();
		}
		
		if (Map.class.isAssignableFrom(typeClass)) {
			return mapToJSON((Map) obj, type.getGenericTypeArguments()[0], type.getGenericTypeArguments()[1]);
		}
		
		if (typeClass.isPrimitive() || Number.class.isAssignableFrom(typeClass) || (typeClass == Boolean.class)) {
			return obj;
		}
		
		throw new JSOXInvalidTypeException("invalid object '%s' for given type '%s' (generics: %s)".formatted(obj, type, ((null != type.getGenericTypeArguments()) ? Arrays.asList(type.getGenericTypeArguments()) : "none")));
	}
	
	public static JSOXVariant decodeObject (final byte[] encodedObject, final Charset charset) throws JSOXException {
		final IOByteBuffer buffer;
		
		buffer = new IOByteBuffer(encodedObject.length);
		buffer.charset(charset);
		buffer.putBytes(encodedObject);
		buffer.rewind();
		
		return decodeObject(buffer);
	}
	
	public static JSOXVariant decodeObject (final IOByteBuffer buffer) throws JSOXException {
		final byte[] data;
		final int    dataSize;
		
		if (buffer.length() < Integer.BYTES) {
			throw new IncompleteObjectException();
		}
		
		dataSize = buffer.getInteger();
		
		if (dataSize <= 0) {
			throw new MalformedObjectException();
		}
		
		if (buffer.length() < (Integer.BYTES + dataSize)) {
			throw new IncompleteObjectException();
		}
		
		data = buffer.getBytes(dataSize);
		
		try {
			return new JSOXVariant(new String(data, buffer.charset()));
		} catch (final JSONException ex) {
			throw new JSOXException(ex.getMessage() + ": " + buffer.toString());
		}
	}
	
	public static byte[] encodeObject (final JSOXObject object, final Charset charset) throws JSOXException {
		return encodeObject(object.toVariant(), charset);
	}
	
	public static byte[] encodeObject (final JSOXVariant object, final Charset charset) throws JSOXException {
		final IOByteBuffer buffer;
		final byte[]       data;
		final int          packetSize;
		
		data = object.toJSONStringCompact().getBytes(charset);
		
		packetSize = (Integer.BYTES + data.length);
		
		buffer = new IOByteBuffer(packetSize);
		buffer.charset(charset);
		buffer.putInteger(data.length);
		buffer.putBytes(data);
		buffer.rewind();
		
		return buffer.toByteArray(charset);
	}
	
	public static String enumToString (final Enum<?> enumValue){
		return enumValue.name();
	}
	
	public static String getJavaSyntaxString (final Class<?> jclass){
		assert(null != jclass);
		
		if (jclass.isArray()) {
			return (jclass.getComponentType().getName() + "[]");
		}
		
		return jclass.getName();
	}
	
	public static String[] getSerializableFields (final Class<? extends JSOXObject> jclass){
		final Set<JSOXColumn> columns;
		int                   cursor;
		final JSOXFactory     factory;
		final String[]        fields;
		
		factory = JSOXFactory.getJSOXFactory(jclass);
		
		columns = factory.columns();
		
		fields = new String[columns.size()];
		
		cursor = 0;
		
		for (final var column : columns) {
			fields[cursor] = column.key();
			
			cursor++;
		}
		
		return fields;
	}
	
	public static boolean isArrayString (final String string){
		return (string.startsWith("[") && string.endsWith("]"));
	}
	
	public static boolean isListString (final String string){
		return (string.startsWith("[") && string.endsWith("]"));
	}
	
	public static JSOXArray listToJSON (final List<?> collection, final ReflectionType elementType) throws JSOXInvalidTypeException {
		final JSOXArray wrapper;
		
		wrapper = new JSOXArray();
		
		for (final var element : collection) {
			wrapper.add(castToJSON(element, elementType));
		}
		
		return wrapper;
	}
	
	public static <K, V> JSONObject mapToJSON (final Map<K, V> map, final ReflectionType keyType, final ReflectionType valueType){
		final JSONObject wrapper;
		
		wrapper = new JSONObject();
		
		for (final var key : map.keySet()) {
			wrapper.put(Objects.toString(castToJSON(key, keyType)), castToJSON(map.get(key), valueType));
		}
		
		return wrapper;
	}
	
	public static <T> T[] parseArrayString (final String arrayString, final ReflectionType type) throws JSOXInvalidTypeException {
		final T[]       array;
		final JSOXArray arrayJSON;
		
		if (! isArrayString(arrayString)) {
			final Iterator<String> it;
			final StringBuilder    sb;
			
			it = StringUtilities.splitStringIntoItemIterator(arrayString, ',');

			sb = new StringBuilder();
			sb.append("[");

			while (it.hasNext()) {
				final String elementString;

				elementString = it.next();

				sb.append(elementString);

				if (it.hasNext()) {
					sb.append(",");
					sb.append(" ");
				}
			}

			sb.append("]");

			return parseArrayString(sb.toString(), type);
		} else {
			arrayJSON = new JSOXArray(arrayString);

			array = (T[]) Array.newInstance(type.getTypeClass(), arrayJSON.size());

			for (var i = 0; i < arrayJSON.size(); i++) {
				final T      element;
				final Object elementJSON;
				final String elementString;

				elementJSON = arrayJSON.get(i);

				elementString = Objects.toString(elementJSON);

				if (elementString.equalsIgnoreCase("null")) 
					element = null;
				else {
					element = (T) castFromString(elementString, type);
				}

				array[i] = element;
			}

			return array;
		}
	}
	
	public static <T extends Enum> T parseEnumString (final Class<T> enumClass, final String enumString){
		return (T) Enum.valueOf(enumClass, enumString.replace(' ', '_').replace('-', '_').replace('.', '_').toUpperCase());
	}
	
	public static <T> List<T> parseListString (final String arrayString, final ReflectionType elementType) throws JSOXInvalidTypeException {
		try {
			final JSOXArray arrayJSON;
			final List<T>   collection;
			
			if (! isListString(arrayString)) {
				final Iterator<String> it;
				final StringBuilder    sb;
				
				it = StringUtilities.splitStringIntoItemIterator(arrayString, ',');
				
				sb = new StringBuilder();
				sb.append("[");
				
				while (it.hasNext()) {
					final String elementString;
					
					elementString = it.next();
					
					sb.append(elementString);
					
					if (it.hasNext()) {
						sb.append(",");
						sb.append(" ");
					}
				}
				
				sb.append("]");
				
				return parseListString(sb.toString(), elementType);
			} else {
				collection = new ArrayList<>();
				
				arrayJSON = new JSOXArray(arrayString);

				for (var i = 0; i < arrayJSON.size(); i++) {
					final T      element;
					final Object elementJSON;
					final String elementString;

					elementJSON = arrayJSON.get(i);

					elementString = Objects.toString(elementJSON);

					if (elementString.equalsIgnoreCase("null")) 
						element = null;
					else {
						element = (T) castFromString(elementString, elementType);
					}
					
					collection.add(element);
				}
			}
			
			return collection;
		} catch (final JSONException ex) {
			throw new JSONException("Invalid %s list string '%s'".formatted(elementType, arrayString), ex);
		}
	}
	
	public static <K, V> Map<K, V> parseMapString (final String mapString, final ReflectionType keyType, final ReflectionType valueType){
		final Map<K, V>  map;
		final JSONObject wrapper;
		
		wrapper = new JSONObject(mapString);
		
		map = new HashMap<>();
		
		for (final String keyString : wrapper.keySet()) {
			final String valueString;
			
			valueString = Objects.toString(wrapper.get(keyString));
			
			map.put((K) castFromString(keyString, keyType), (V) castFromString(valueString, valueType));
		}
		
		return map;
	}
	
	public static Object transform (final String string){
		if (string.equals("null")) {
			return null;
		}
		
		if (StringUtilities.isBoolean(string)) {
			return BooleanUtilities.fromString(string);
		}
		
		if (StringUtilities.isInteger(string)) {
			return Integer.parseInt(string);
		}
		
		if (StringUtilities.isLong(string)) {
			return Long.parseLong(string);
		}
		
		if (StringUtilities.isDouble(string)) {
			return Double.parseDouble(string);
		}
		
		if (string.startsWith("{") && string.endsWith("}")) try {
			return new JSOXVariant(string);
		} catch (final Throwable t) {
			Exceptions.ignore(t);
		}
		
		return string;
	}
	
	public static Object transform (final List<String> options){
		final JSOXArray array;
		
		if (options.size() == 1) {
			return transform(options.get(0));
		}
		
		array = new JSOXArray();
		
		for (final var option : options) {
			array.add(transform(option));
		}
		
		return array;
	}
	
	public static JSOXVariant transform (final Map<String, List<String>> map){
		final JSOXVariant transform;
		
		transform = new JSOXVariant();
		
		for (final var entry : map.entrySet()) {
			final String       key;
			final List<String> options;
			final Object       value;
			
			key = entry.getKey();
			
			options = entry.getValue();
			
			if (options.isEmpty()) 
				continue;
			
			value = transform(options);
			
			transform.putDirect(key, value);
		}
		
		return transform;
	}
	
	public static String translateJavaFieldToJSONField (final String javaField){
		final StringBuilder sb;

		sb = new StringBuilder();

		for (var i = 0; i < javaField.length(); i++) {
			final char c;

			c = javaField.charAt(i);

			if (Character.isLowerCase(c)) {
				sb.append(c);
			} else {
				if ((i > 0) && ((i + 1) < javaField.length()) && (!Character.isUpperCase(javaField.charAt(i - 1)) || Character.isLowerCase(javaField.charAt(i + 1)))) {
					sb.append('-');
				}

				sb.append(Character.toLowerCase(c));
			}
		}

		return sb.toString();
	}
	
	public static String translateJSONFieldToJavaField (final String jsonField){
		final StringBuilder sb;

		sb = new StringBuilder();

		for (var i = 0; i < jsonField.length(); i++) {
			char c;

			c = jsonField.charAt(i);
			
			if (c == '-') {
				if ((i + 1) < jsonField.length()) {
					i++;
					
					c = jsonField.charAt(i);
					
					sb.append(Character.toUpperCase(c));
				} else {
					break;
				}
			} else {
				sb.append(Character.toLowerCase(c));
			}
		}

		return sb.toString();
	}
	
	
	
	private JSOXUtilities (){
		throw new UnsupportedOperationException("Not supported.");
	}
}