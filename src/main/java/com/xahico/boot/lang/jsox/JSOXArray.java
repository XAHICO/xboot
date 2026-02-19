/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.lang.jsox;

import com.xahico.boot.util.Parameters;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class JSOXArray implements JSOXCollection, List {
	public static JSOXArray wrap (final Object[] array){
		return JSOXArray.wrap(Arrays.asList(array));
	}
	
	public static JSOXArray wrap (final List<?> collection){
		return new JSOXArray().siphon(collection);
	}
	
	
	
	private JSONArray internalObject;
	
	
	
	public JSOXArray (){
		this(new JSONArray());
	}
	
	public JSOXArray (final JSONArray internalObject){
		super();
		
		this.internalObject = internalObject;
	}
	
	public JSOXArray (final String internalObject){
		this(new JSONArray(internalObject));
	}
	
	
	
	public void append (final Object obj){
		if (null == obj) 
			this.internalObject.put("null");
		else if (obj.getClass() == JSOXObject.class) 
			this.internalObject.put(((JSOXObject)obj).json());
		else if (obj.getClass() == JSOXVariant.class) 
			this.internalObject.put(((JSOXVariant)obj).json());
		else if (List.class.isAssignableFrom(obj.getClass())) 
			this.internalObject.put(JSOXArray.wrap(((List<?>)obj)).json());
		else if (Map.class.isAssignableFrom(obj.getClass())) 
			this.internalObject.put(JSOXMap.wrap(((Map<String, ?>)obj)).json());
		else {
			this.internalObject.put(obj);
		}
	}
	
	@Override
	public void clear (){
		this.internalObject.clear();
	}
	
	@Override
	public JSOXArray copy (){
		return new JSOXArray(new JSONArray(this.internalObject));
	}
	
	public JSONArray json (){
		return this.internalObject;
	}
	
	@Override
	public Parameters parameterize (){
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
	public JSOXArray siphon (final List<?> collection){
		for (final var element : collection) {
			this.append(element);
		}
		
		return JSOXArray.this;
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
	public boolean contains (final Object lookup){
		for (final var obj : this.internalObject) {
			if (obj == lookup) {
				return true;
			}
		}
		
		return false;
	}

	@Override
	public Iterator<?> iterator (){
		return this.internalObject.iterator();
	}

	@Override
	public Object[] toArray (){
		final Object[] array;
		
		array = new Object[this.internalObject.length()];
		
		for (var i = 0; i < array.length; i++) {
			array[i] = this.internalObject.get(i);
		}
		
		return array;
	}

	@Override
	public Object[] toArray (final Object[] a){
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public boolean add (final Object e){
		this.internalObject.put((null != e) ? e : JSONObject.NULL);
		
		return true;
	}
	
	@Override
	public boolean remove (final Object o){
		for (var i = 0; i < this.internalObject.length(); i++) {
			if (this.internalObject.get(i) == o) {
				this.internalObject.remove(i);
				
				return true;
			}
		}
		
		return false;
	}

	@Override
	public boolean containsAll (final Collection c){
		for (final var o : c) {
			if (! this.contains(o)) {
				return false;
			}
		}
		
		return true;
	}

	@Override
	public boolean addAll (final Collection c){
		boolean all = true;
		
		for (final var o : c) {
			if (! this.add(o)) {
				all = false;
			}
		}
		
		return all;
	}

	@Override
	public boolean addAll (final int index, final Collection c){
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public boolean removeAll (final Collection c){
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public boolean retainAll(Collection c) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public Object get (final int index){
		final Object obj;
		
		obj = this.internalObject.get(index);
		
		if (obj instanceof JSONArray obja) 
			return new JSOXArray(obja);
		
		if (obj instanceof JSONObject obja) 
			return new JSOXVariant(obja);
		
		return obj;
	}

	@Override
	public Object set (final int index, final Object element){
		final Object old;
		
		old = this.internalObject.get(index);
		
		this.internalObject.put(index, element);
		
		return old;
	}

	@Override
	public void add (final int index, final Object element){
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public Object remove (final int index){
		final Object old;
		
		old = this.internalObject.get(index);
		
		this.internalObject.remove(index);
		
		return old;
	}

	@Override
	public int indexOf (final Object o){
		for (var i = 0; i < this.internalObject.length(); i++) {
			if (this.internalObject.get(i) == o) {
				return i;
			}
		}
		
		return -1;
	}

	@Override
	public int lastIndexOf (final Object o){
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public ListIterator listIterator (){
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public ListIterator listIterator (final int index) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public List subList (final int fromIndex, final int toIndex){
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
}