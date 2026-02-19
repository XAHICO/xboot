/*
 * Written by Tuomas Kontiainen <https://www.github.com/tutomiko>
 * 
 * Copyright (c) 2023, Tuomas Kontiainen
 */
package com.xahico.boot.lang.jsox; 

import com.xahico.boot.reflection.ReflectionType;
import com.xahico.boot.util.ArrayUtilities;
import com.xahico.boot.util.CollectionUtilities;
import com.xahico.boot.util.Parameters;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.json.JSONObject;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public class JSOXObject implements JSOX, JSOXCastable, Map<String, Object> {
	public static <T extends JSOXObject> T loadFromFile (final Class<T> jclass, final File file) throws IOException {
		return JSOXObject.loadFromFile(jclass, file.toPath());
	}
	
	public static <T extends JSOXObject> T loadFromFile (final Class<T> jclass, final File file, final Charset charset) throws IOException {
		return JSOXObject.loadFromFile(jclass, file.toPath(), charset);
	}
	
	public static <T extends JSOXObject> T loadFromFile (final Class<T> jclass, final Path filePath) throws IOException {
		return JSOXObject.newInstanceOf(jclass, new JSOXVariant(Files.readString(filePath)));
	}
	
	public static <T extends JSOXObject> T loadFromFile (final Class<T> jclass, final Path filePath, final Charset charset) throws IOException {
		return JSOXObject.newInstanceOf(jclass, new JSOXVariant(Files.readString(filePath, charset)));
	}
	
	public static <T extends JSOXObject> T newInstanceOf (final Class<T> jclass){
		final JSOXFactory<T> factory;
		
		factory = JSOXFactory.getJSOXFactory(jclass);
		
		return factory.newInstance();
	}
	
	public static <T extends JSOXObject> T newInstanceOf (final Class<T> jclass, final byte[] json, final Charset charset){
		return JSOXObject.newInstanceOf(jclass, new JSONObject(new String(json, charset)));
	}
	
	public static <T extends JSOXObject> T newInstanceOf (final Class<T> jclass, final JSOXMap data){
		return JSOXObject.newInstanceOf(jclass, data.json());
	}
	
	public static <T extends JSOXObject> T newInstanceOf (final Class<T> jclass, final JSONObject json){
		final JSOXFactory<T> factory;
		
		factory = JSOXFactory.getJSOXFactory(jclass);
		
		if (null == factory) {
			throw new Error("No factory for %s (instanciate with '%s')".formatted(jclass, json));
		}
		
		return factory.newInstance(json);
	}
	
	public static <T extends JSOXObject> T newInstanceOf (final Class<T> jclass, final JSOXVariant jsox){
		return newInstanceOf(jclass, jsox.json());
	}
	
	public static <T extends JSOXObject> T newInstanceOf (final Class<T> jclass, final Map<String, ?> data){
		return JSOXObject.newInstanceOf(jclass, JSOXMap.wrap(data));
	}
	
	public static <T extends JSOXObject> T newInstanceOf (final Class<T> jclass, final String json){
		return JSOXObject.newInstanceOf(jclass, new JSONObject(json));
	}
	
	
	
	@JSOXTransient
	private final JSOXFactory<?> factory = JSOXFactory.getJSOXFactory(this.getClass());
	
	
	
	public JSOXObject (){
		super();
	}
	
	
	
	@Override
	public final void assume (final JSOX obj, final boolean reset){
		if (reset) {
			this.clear();
		}
		
		this.json(obj.json());
	}
	
	@Override
	public final void assume (final String obj, final boolean reset){
		if (reset) {
			this.clear();
		}
		
		this.json(new JSONObject(obj));
	}
	
	@Override
	public final <T extends JSOXObject> T castTo (final Class<T> jclass){
		return JSOXObject.newInstanceOf(jclass, this.json());
	}
	
	@Override
	public final void clear (){
		this.json(new JSONObject());
	}
	
	@Override
	public final JSOXObject copy (){
		return JSOXObject.newInstanceOf(this.getClass(), this.toVariant());
	}
	
	@Override
	public final void copyTo (final JSOX other){
		other.json(this.json());
	}
	
	@Override
	public boolean equals (final Object obj){
		final JSOXObject other;
		
		if (this == obj) 
			return true;
		
		if (null == obj) 
			return false;
		
		if (!(obj instanceof JSOXObject)) 
			return false;
		
		if (! this.getClass().isAssignableFrom(obj.getClass())) {
			return false;
		}
		
		other = (JSOXObject)(obj);
		
		for (final var column : this.factory.columns()) {
			final Object columnValue1;
			final Object columnValue2;
			
			columnValue1 = column.get(this);
			columnValue2 = column.get(other);
			
			if (! Objects.equals(columnValue1, columnValue2)) {
				return false;
			}
		}
		
		return true;
	}
	
	@Override
	public final Object get (final String key){
		return this.getJSONField(key);
	}
	
	final JSOXFactory<?> getFactory (){
		return this.factory;
	}
	
	public final Object getJSONField (final String keyJava){
		final JSOXColumn column;
		final Object     value;
		
		column = this.getFactory().column(keyJava);
		
		if (null == column) {
			return null;
		}
		
		value = column.get(this);
		
		return value;
	}
	
	@Override
	public int hashCode () {
		int result = this.getClass().hashCode();
		
		for (final var column : this.factory.columns()) {
			final Object value = column.get(this);
			
			result = 31 * result + Objects.hashCode(value);
		}
		
		return result;
	}
	
	@Override
	public final JSONObject json () throws JSOXSerializationException {
		final JSONObject json;
		
		json = new JSONObject();
		
		for (final JSOXColumn column : this.getFactory().columns()) try {
			final Class<?>       actualType;
			final ReflectionType genericType;
			final String         keyJava;
			final String         keyJSON;
			final Object         value;
			
			keyJava = column.key();
			keyJSON = JSOXUtilities.translateJavaFieldToJSONField(keyJava);
			
			value = column.get(this);
			
			actualType = column.actualType();
			
			genericType = column.genericType();
			
			json.put(keyJSON, JSOXUtilities.castToJSON(value, ((null != genericType) ? genericType : ReflectionType.genericType(actualType))));
		} catch (final JSOXInvalidTypeException ex) {
			throw new JSOXSerializationException(ex);
		}
		
		return json;
	}
	
	@Override
	public final void json (final JSONObject json) throws JSOXSerializationException {
		final Iterator<String> it;
		
		it = json.keys();
		
		while (it.hasNext()) try {
			final Class<?>       actualType;
			final JSOXColumn     column;
			final ReflectionType genericType;
			final String         keyJava;
			final String         keyJSON;
			final Object         value;
			
			keyJSON = it.next();
			
			keyJava = JSOXUtilities.translateJSONFieldToJavaField(keyJSON);
			
			column = this.getFactory().column(keyJava);
			
			if (null == column) 
				continue;
			
			actualType = column.actualType();
			
			genericType = column.genericType();
			
			value = json.get(keyJSON);
			
			column.set(this, JSOXUtilities.castFromString(Objects.toString(value), ((null != genericType) ? genericType : ReflectionType.genericType(actualType))));
			
//			column.set(this, JSOXUtilities.castFromString(value, actualType, ((null != genericType) ? genericType.getGenericTypeArgumentClasses() : new Class[0])));
		} catch (final JSOXInvalidTypeException ex) {
			throw new JSOXSerializationException(ex);
		}
	}
	
	@Override
	public final Set<String> keySet (){
		final Set<String> collection;
		
		collection = new HashSet<>();
		
		for (final var column : this.getFactory().columns()) {
			collection.add(column.key());
		}
		
		return collection;
	}
	
	@Override
	public final Parameters parameterize (){
		final Parameters parameters;
		
		parameters = new Parameters();
		
		for (final var column : this.getFactory().columns()) {
			final String key;
			final Object value;
			
			value = column.get(this);
			
			if (null == value) 
				continue;
			
			key = column.key();
			
			parameters.append(JSOXUtilities.translateJavaFieldToJSONField(key));
			
			if (column.actualType().isArray()) {
				parameters.append(key, ArrayUtilities.transformObjectArrayToStringArray((Object[]) value));
			} else if (List.class.isAssignableFrom(column.actualType())) {
				parameters.append(key, CollectionUtilities.transformObjectLinkedList((List)value, (element) -> element.toString()));
			} else {
				parameters.append(key, Objects.toString(value));
			}
		}
		
		return parameters;
	}
	
	private String print (final int depth){
		final List<JSOXColumn> columns;
		int                    longestType;
		final StringBuilder    sb;
		
		sb = new StringBuilder();
		sb.append(this.getClass().getName());
		sb.append(" ");
		sb.append("{");
		
		longestType = 0;
		
		columns = new ArrayList<>();
		columns.addAll(this.getFactory().columns());
		columns.sort((o1, o2) -> o1.key().toLowerCase().compareTo(o2.key().toLowerCase()));
		
		for (final var column : columns) {
			final int    typeLength;
			final String typeName;
			
			typeName = column.pattern();
			typeLength = typeName.length();
			
			if (typeLength > longestType) {
				longestType = typeLength;
			}
		}
		
		if (! columns.isEmpty()) {
			sb.append(System.lineSeparator());
			
			for (final var column : columns) {
				final int    typeLength;
				final String typeName;
				final Object value;
				
				typeName = column.pattern();
				typeLength = typeName.length();
				
				value = column.get(this);
				
				sb.append("\t".repeat(depth));
				sb.append("\t");
				sb.append(typeName);
				sb.append(" ".repeat((((longestType - typeLength) > 0) ? (longestType - typeLength) : 0) + 1));
				sb.append(column.key());
				sb.append(" = ");
				
				if (column.actualType().isArray()) {
					if (null == value) 
						sb.append("null");
					else {
						sb.append(column.actualType().getComponentType().getName())
						  .append(Arrays.asList((Object[]) value));
					}
				}
				else if (value instanceof Character) 
					sb.append("\'").append(value).append("\'");
				else if (value instanceof JSOXObject) 
					sb.append(((JSOXObject) value).print(depth + 1));
				else if (value instanceof String) 
					sb.append("\"").append(value).append("\"");
				else if (value instanceof Enum) 
					sb.append(value.getClass().getName()).append(".").append(((Enum<?>)value).name());
				else {
					sb.append(value);
				}
				
				sb.append(";");
				sb.append(System.lineSeparator());
			}
			
			sb.append("\t".repeat(depth));
		}
		
		sb.append("}");
		
		return sb.toString();
	}
	
	public final boolean setJSONField (final String keyJava, final Object value){
		final JSOXColumn column;
		
		column = this.getFactory().column(keyJava);
		
		if (null == column) {
			return false;
		}
		
		column.set(this, value);
		
		return true;
	}
	
	public final boolean setJSONField (final String keyJava, final String value){
		final Class<?>       actualType;
		final JSOXColumn     column;
		final ReflectionType genericType;
		
		column = this.getFactory().column(keyJava);
		
		if (null == column) {
			return false;
		}
		
		actualType = column.actualType();
		
		genericType = column.genericType();
		
		column.set(this, JSOXUtilities.castFromString(value, ((null != genericType) ? genericType : ReflectionType.genericType(actualType))));
		
		return true;
	}
	
	public final String toJavaString (){
		return this.print(0);
	}
	
	@Override
	public final String toJSONString (){
		return this.json().toString(4);
	}
	
	@Override
	public final String toJSONStringCompact (){
		return this.json().toString();
	}
	
	@Deprecated
	public final String toJSONStringRaw (){
		return this.json().toString();
	}
	
	@Override
	public String toString (){
		return this.toJSONStringCompact();
	}
	
	public final JSOXVariant toVariant (){
		return new JSOXVariant(this);
	}
	
	@Override
	public int size (){
		return this.factory.columns().size();
	}
	
	@Override
	public boolean isEmpty (){
		return (this.size() == 0);
	}
	
	@Override
	public boolean containsKey (final Object o){
		if (!(o instanceof String)) 
			return false;
		else {
			return this.factory.has((String)(o));
		}
	}

	@Override
	public boolean containsValue (final Object o){
		return false;
	}

	@Override
	public Object get (final Object o){
		if (!(o instanceof String)) 
			return false;
		else {
			final JSOXColumn column;
			
			column = this.factory.column((String)(o));
			
			if (null == column) 
				return false;
			else {
				return column.get(this);
			}
		}
	}

	@Override
	public Object put (final String key, final Object value){
		final JSOXColumn column;

		column = this.factory.column(key);

		if (null == column) 
			throw new UnsupportedOperationException("Not supported.");
		else {
			final Object valueLast;

			valueLast = column.get(this);

			column.set(this, value);

			return valueLast;
		}
	}

	@Override
	public Object remove (final Object o){
		throw new UnsupportedOperationException("Not supported.");
	}
	
	@Override
	public Collection<Object> values (){
		final List<Object> collection;
		
		collection = new ArrayList<>(this.factory.columns().size());
		
		for (final var column : this.factory.columns()) {
			collection.add(column.get(this));
		}
		
		return collection;
	}

	@Override
	public Set<Map.Entry<String, Object>> entrySet (){
		final Set<Map.Entry<String, Object>> set;
		
		set = new HashSet<>();
		
		for (final var column : this.factory.columns()) {
			final String columnKey;
			final Object columnValue;
			
			columnKey = column.key();
			
			columnValue = column.get(this);
			
			set.add(new AbstractMap.SimpleEntry<>(columnKey, columnValue));
		}
		
		return set;
	}

	@Override
	public void putAll (final Map<? extends String, ? extends Object> m){
		for (final var key : m.keySet()) {
			this.put(key, m.get(key));
		}
	}
}