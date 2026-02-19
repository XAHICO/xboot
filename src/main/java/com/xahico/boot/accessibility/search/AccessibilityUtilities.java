/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.accessibility.search;

import com.xahico.boot.util.StringUtilities;
import com.xahico.boot.util.transformer.ObjectTransformer;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class AccessibilityUtilities {
	public static AccessibleProperties createDefaultProperties (final Accessible accessible){
		return new AccessibleProperties() {
			final AccessibilityProvider provider = AccessibilityProvider.createAccessibilityProvider(accessible);
			
			@Override
			public AccessibleProperty get (final String key) throws NoSuchPropertyException {
				return provider.getProperty(key);
			}
			
			@Override
			public String[] keys (){
				return provider.getPropertyKeys();
			}
		};
	}
	
	public static String encodeSearchString (final String searchString, final String context){
		return Integer.toHexString(Objects.hashCode(context + "::" + searchString));
	}
	
	public static <E> String listToReadableString (final Collection<E> collection){
		return listToReadableString(collection, (element) -> Objects.toString(element));
	}
	
	public static <E> String listToReadableString (final Collection<E> collection, final ObjectTransformer<E, String> transformer){
		final Iterator<E>   it;
		final StringBuilder sb;
		
		sb = new StringBuilder();
		
		it = collection.iterator();
		
		while (it.hasNext()) {
			final E element;
			
			element = it.next();
			
			sb.append(StringUtilities.quote(transformer.call(element)));
			
			if (it.hasNext()) {
				sb.append(" ");
			}
		}
		
		return sb.toString();
	}
	
	
	
	private AccessibilityUtilities (){
		throw new UnsupportedOperationException("Not supported.");
	}
}