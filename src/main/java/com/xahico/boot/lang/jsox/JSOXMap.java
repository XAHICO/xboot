/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.lang.jsox;

import com.xahico.boot.util.Parameters;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class JSOXMap implements JSOXCollection, Map<String, Object> {
	public static JSOXMap wrap (final Map<String, ?> collection){
		return new JSOXMap().siphon(collection);
	}
	
	
	
	private JSONObject internalObject;
	
	
	
	public JSOXMap (){
		this(new JSONObject());
	}
	
	public JSOXMap (final JSONObject internalObject){
		super();
		
		this.internalObject = internalObject;
	}
	
	
	
	@Override
	public void clear (){
		this.internalObject.clear();
	}
	
	@Override
	public JSOXMap copy (){
		return new JSOXMap(new JSONObject(this.internalObject.toString()));
	}
	
	public JSONObject json (){
		return this.internalObject;
	}
	
	@Override
	public Parameters parameterize (){
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
	public void putDirect (final String key, final Object obj){
		if (null == obj) 
			this.internalObject.put(key, JSONObject.NULL);
		else if (obj.getClass() == JSOXObject.class) 
			this.internalObject.put(key, ((JSOXObject)obj).json());
		else if (obj.getClass() == JSOXVariant.class) 
			this.internalObject.put(key, ((JSOXVariant)obj).json());
		else if (List.class.isAssignableFrom(obj.getClass())) 
			this.internalObject.put(key, JSOXArray.wrap(((List<?>)obj)).json());
		else if (Map.class.isAssignableFrom(obj.getClass())) 
			this.internalObject.put(key, JSOXMap.wrap(((Map<String, ?>)obj)).json());
		else {
			this.internalObject.put(key, obj);
		}
	}
	
	public JSOXMap siphon (final Map<String, ?> collection){
		for (final var key : collection.keySet()) {
			this.put(key, collection.get(key));
		}
		
		return JSOXMap.this;
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
			valueLast = this.internalObject.get(key);
		} else {
			valueLast = null;
		}
		
		this.putDirect(key, value);
		
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
			value = this.internalObject.get(key);
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
				
				keyIter = JSOXMap.this.keySet().iterator();
				
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
			    return JSOXMap.this.size();
			}
		};
	}
	
	@Override
	public Set<String> keySet (){
		return this.internalObject.keySet();
	}
	
	public JSOXVariant toVariant (){
		return new JSOXVariant(this.internalObject);
	}
}