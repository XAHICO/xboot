/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.accessibility.search;

import com.xahico.boot.reflection.Reflection;
import java.util.HashSet;
import java.util.Set;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class AccessibilityProvider {
	public static AccessibilityProvider createAccessibilityProvider (final Accessible accessible){
		final Set<AccessibleProperty> propertyList;
		final Reflection<?>           reflection;
		
		reflection = Reflection.of(accessible.getClass());
		
		propertyList = new HashSet<>();
		
		for (final var field : reflection.getAllFields()) {
			final AccessibleProperty property;
			
			if (! AccessibleProperty.class.isAssignableFrom(field.getType())) 
				continue;
			
			property = (AccessibleProperty) field.get(accessible);
			
			propertyList.add(property);
		}
		
		return new AccessibilityProvider(propertyList);
	}
	
	
	
	private final Set<AccessibleProperty> propertyList;
	
	
	
	private AccessibilityProvider (final Set<AccessibleProperty> propertyList){
		super();
		
		this.propertyList = propertyList;
	}
	
	
	
	public AccessibleProperty getProperty (final String key) throws NoSuchPropertyException {
		for (final var property : this.propertyList) {
			if (property.key().equalsIgnoreCase(key)) {
				return property;
			}
		}
		
		throw new NoSuchPropertyException();
	}
	
	public String[] getPropertyKeys (){
		int            cursor;
		final String[] keyArray;
		
		keyArray = new String[this.propertyList.size()];
		
		cursor = 0;
		
		for (final var property : this.propertyList) {
			keyArray[cursor] = property.key();
			
			cursor++;
		}
		
		return keyArray;
	}
	
	public Set<AccessibleProperty> getPropertyList (){
		return this.propertyList;
	}
}