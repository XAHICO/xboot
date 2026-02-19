/*
 * Written by Tuomas Kontiainen <https://www.github.com/tutomiko>
 * 
 * Copyright (c) 2023, Tuomas Kontiainen
 */
package com.xahico.boot.lang.jsox;

import com.xahico.boot.util.ArrayUtilities;
import com.xahico.boot.util.CollectionUtilities;
import com.xahico.boot.util.Parameters;
import java.nio.charset.Charset;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class JSOXVariant implements JSOX, JSOXCastable, Map<String, Object> {
	private JSONObject internalObject;
	
	
	
	public JSOXVariant (){
		super();
		
		this.internalObject = new JSONObject();
	}
	
	public JSOXVariant (final JSONObject json){
		this();
		
		this.internalObject = json;
	}
	
	public JSOXVariant (final JSOXObject jsox){
		this(jsox.json());
	}
	
	public JSOXVariant (final Map<String, Object> map){
		this();
		
		for (final var entry : map.entrySet()) {
			this.internalObject.put(entry.getKey(), entry.getValue());
		}
	}
	
	public JSOXVariant (final String json){
		this(new JSONObject(json));
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
	public <T extends JSOXObject> T castTo (final Class<T> jclass){
		return JSOXObject.newInstanceOf(jclass, this.internalObject);
	}
	
	@Override
	public void clear (){
		this.internalObject.clear();
	}
	
	@Override
	public JSOXVariant copy (){
		return new JSOXVariant(this.internalObject);
	}
	
	@Override
	public void copyTo (final JSOX other){
		other.json(this.json());
	}

	@Override
	public Object get (final String key){
		final Object obj;
		
		obj = this.internalObject.get(key);
		
		if (obj instanceof JSONArray obja) 
			return new JSOXArray(obja);
		
		if (obj instanceof JSONObject obja) 
			return new JSOXVariant(obja);
		
		return obj;
	}
	
	public boolean getBoolean (final String key){
		return this.internalObject.getBoolean(key);
	}
	
	public double getDouble (final String key){
		return this.internalObject.getDouble(key);
	}
	
	public <T extends Enum> T getEnum (final String key, final Class<T> enumClass){
		return JSOXUtilities.parseEnumString(enumClass, this.getString(key));
	}
	
	public int getInteger (final String key){
		return this.internalObject.getInt(key);
	}
	
	public long getLong (final String key){
		return this.internalObject.getLong(key);
	}
	
	public String getString (final String key){
		return this.internalObject.getString(key);
	}
	
	public JSOXVariant getVariant (final String key){
		return new JSOXVariant(this.internalObject.getJSONObject(key));
	}
	
	public boolean has (final String key){
		return this.internalObject.has(key);
	}
	
	@Override
	public JSONObject json (){
		return this.internalObject;
	}
	
	@Override
	public void json (final JSONObject json){
		this.internalObject = json;
	}
	
	@Override
	public Set<String> keySet (){
		return this.internalObject.keySet();
	}
	
	@Override
	public Parameters parameterize (){
		final Parameters parameters;
		
		parameters = new Parameters();
		
		for (final var key : this.keySet()) {
			final Object   value;
			final Class<?> valueType;
			
			value = this.get(key);
			
			if (null == value) 
				continue;
			
			valueType = value.getClass();
			
			parameters.append(JSOXUtilities.translateJavaFieldToJSONField(key));
			
			if (valueType.isArray()) {
				parameters.append(key, ArrayUtilities.transformObjectArrayToStringArray((Object[]) value));
			} else if (List.class.isAssignableFrom(valueType)) {
				parameters.append(key, CollectionUtilities.transformObjectLinkedList((List)value, (element) -> element.toString()));
			} else {
				parameters.append(key, Objects.toString(value));
			}
		}
		
		return parameters;
	}
	
	public void putBoolean (final String key, final boolean value){
		this.internalObject.put(key, value);
	}
	
	public void putDirect (final String key, final Object value){
		if (null == value) 
			this.put(key, JSONObject.NULL);
		else if (value.getClass() == String.class) 
			this.putString(key, ((String)value));
		else if (value.getClass() == JSOXObject.class) 
			this.putObject(key, ((JSOXObject)value).toVariant());
		else if (value.getClass() == JSOXVariant.class) 
			this.putObject(key, ((JSOXVariant)value));
		else if (List.class.isAssignableFrom(value.getClass())) 
			this.putList(key, ((List<?>)value));
		else if (Map.class.isAssignableFrom(value.getClass())) 
			this.putMap(key, ((Map<String, ?>)value));
		else {
			this.internalObject.put(key, value);
		}
	}
	
	public void putInteger (final String key, final int value){
		this.internalObject.put(key, value);
	}
	
	public void putList (final String key, final List<?> collection){
		this.internalObject.put(key, JSOXArray.wrap(collection).json());
	}
	
	public void putMap (final String key, final Map<String, ?> collection){
		this.internalObject.put(key, JSOXMap.wrap(collection).json());
	}
	
	public void putObject (final String key, final JSOXVariant value){
		this.internalObject.put(key, ((null != value) ? value.json() : null));
	}
	
	public void putString (final String key, final String value){
		this.internalObject.put(key, ((null != value) ? value : JSONObject.NULL));
	}
	
	public void siphon (final Map<String, ?> collection){
		for (final var keyObject : collection.keySet()) {
			this.put(keyObject, collection.get(keyObject));
		}
	}
	
	public String toString (final Charset charset){
		return new String(this.toString().getBytes(), charset);
	}
	
	@Override
	public String toJSONString (){
		return this.internalObject.toString(4);
	}
	
	@Override
	public String toJSONStringCompact (){
		return this.internalObject.toString();
	}
	
	@Override
	public String toString (){
		return this.toJSONStringCompact();
	}
	
	
	@Override
	public int size (){
		return this.internalObject.length();
	}
	
	@Override
	public boolean isEmpty (){
		return this.internalObject.isEmpty();
	}
	
	@Override
	public boolean containsKey (final Object o){
		if (o instanceof String) 
			return this.internalObject.has((String)(o));
		else {
			return false;
		}
	}

	@Override
	public boolean containsValue (final Object o){
		for (final var key : this.internalObject.keySet()) {
			if (this.internalObject.get(key) == o) {
				return true;
			}
		}
		
		return false;
	}

	@Override
	public Object get (final Object o){
		final Object obj;
		
		if (!(o instanceof String)) 
			return false;
		
		obj = this.internalObject.get((String)(o));
		
		if (obj instanceof JSONArray obja) 
			return new JSOXArray(obja);
		
		if (obj instanceof JSONObject obja) 
			return new JSOXVariant(obja);
		
		return obj;
	}

	@Override
	public Object put (final String key, final Object value){
		final Object valueLast;
		
		if (this.internalObject.has(key)) {
			valueLast = this.get(key);
		} else {
			valueLast = null;
		}
		
		this.putDirect(key, ((null != value) ? value : JSONObject.NULL));
		
		return valueLast;
	}

	@Override
	public Object remove (final Object o){
		final String key;
		final Object value;
		
		if (!(o instanceof String)) {
			return false;
		}
		
		key = (String)(o);
		
		if (this.internalObject.has(key)) {
			value = this.get(key);
		} else {
			value = null;
		}
		
		this.internalObject.remove(key);
		
		return value;
	}

	@Override
	public void putAll (final Map<? extends String, ? extends Object> m){
		for (final var key : m.keySet()) {
			this.put(key, m.get(key));
		}
	}

	@Override
	public Collection<Object> values (){
		final List<Object> collection;
		
		collection = new ArrayList<>();
		
		for (final var key : this.keySet()) {
			collection.add(this.get(key));
		}
		
		return collection;
	}

	@Override
	public Set<Entry<String, Object>> entrySet (){
		return new AbstractSet<Entry<String,Object>>() {
			@Override
			public Iterator<Entry<String,Object>> iterator() {
				final Iterator<String> keyIter;
				
				keyIter = JSOXVariant.this.keySet().iterator();
				
				return new Iterator<Entry<String,Object>>() {
					@Override
					public boolean hasNext (){
						return keyIter.hasNext();
					}
					
					@Override
					public Entry<String,Object> next (){
						final String key;
						final Object value;
						
						key = keyIter.next();
						value = get(key);
						
						return new AbstractMap.SimpleEntry<>(key, value);
					}
					
					@Override
					public void remove (){
						keyIter.remove();
					}
				};
			}

			@Override
			public int size() {
			    return JSOXVariant.this.size();
			}
		};
	}
}