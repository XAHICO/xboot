/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class Parameters {
	private final Map<String, List<String>> internalsNamed = new HashMap<>();
	private final List<String>              internalsUnnamed = new LinkedList<>();
	private String                          prefix = null;
	
	
	
	public Parameters (){
		super();
	}
	
	
	
	public Parameters append (final List<String> itemList){
		this.internalsUnnamed.addAll(itemList);
		
		return Parameters.this;
	}
	
	public Parameters append (final String item){
		this.internalsUnnamed.add(item);
		
		return Parameters.this;
	}
	
	public Parameters append (final String... itemArray){
		return this.append(Arrays.asList(itemArray));
	}
	
	public Parameters append (final String key, final Collection<String> itemList){
		final List<String> valueList;
		
		valueList = this.getOrCreate(key);
		valueList.addAll(itemList);
		
		return Parameters.this;
	}
	
	public Parameters append (final String key, final String item){
		final List<String> valueList;
		
		valueList = this.getOrCreate(key);
		valueList.add(item);
		
		return Parameters.this;
	}
	
	public Parameters append (final String key, final String... itemArray){
		return this.append(key, Arrays.asList(itemArray));
	}
	
	public Parameters clear (){
		this.internalsUnnamed.clear();
		this.internalsNamed.clear();
		
		return Parameters.this;
	}
	
	public int count (){
		return (this.internalsNamed.size() + 1);
	}
	
	public List<String> get (){
		return this.internalsUnnamed;
	}
	
	public List<String> get (final String key){
		return this.internalsNamed.get(key);
	}
	
	public String getFirst (final String key){
		return this.getFirst(key, null);
	}
	
	public String getFirst (final String key, final String defaultValue){
		final String       value;
		final List<String> valueList;
		
		valueList = this.get(key);
		
		if ((null == valueList) || valueList.isEmpty()) {
			return defaultValue;
		}
		
		value = valueList.get(0);
		
		if (null == value) {
			return defaultValue;
		}
		
		return value;
	}
	
	public String getLast (final String key){
		return this.getLast(key, null);
	}
	
	public String getLast (final String key, final String defaultValue){
		final String       value;
		final List<String> valueList;
		
		valueList = this.get(key);
		
		if ((null == valueList) || valueList.isEmpty()) {
			return defaultValue;
		}
		
		value = valueList.get(valueList.size() - 1);
		
		if (null == value) {
			return defaultValue;
		}
		
		return value;
	}
	
	private List<String> getOrCreate (final String key){
		List<String> valueList;
		
		valueList = this.get(key);
		
		if (null == valueList) {
			valueList = new LinkedList<>();
			
			this.internalsNamed.put(key, valueList);
		}
		
		return valueList;
	}
	
	public boolean hasKey (final String key){
		return this.internalsNamed.containsKey(key);
	}
	
	public void setPrefix (final String prefix){
		this.prefix = prefix;
	}
	
	public String[] toArray (){
		final List<String> collection;
		
		collection = this.toList();
		
		return collection.toArray(new String[collection.size()]);
	}
	
	public List<String> toList (){
		final List<String> collection;
		
		collection = new LinkedList<>();
		collection.addAll(this.internalsUnnamed);
		
		for (final var key : this.internalsNamed.keySet()) {
			collection.add((null != this.prefix) ? (this.prefix + key) : key);
			collection.addAll(this.internalsNamed.get(key));
		}
		
		return collection;
	}
}