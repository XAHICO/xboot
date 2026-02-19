/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.net.web.http.model.glass;

import com.xahico.boot.lang.html.HTMLDocument;
import com.xahico.boot.lang.html.HTMLSpecialElement;
import com.xahico.boot.util.Exceptions;
import com.xahico.boot.util.StringUtilities;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
final class GlassClassLoader {
	private static String extractClassString (final String string) throws InvalidClassStringException {
		final Iterator<String> it;
		
		it = StringUtilities.splitStringByWhitespaceIntoIterator(string);
		
		if (!it.hasNext() || !it.next().equalsIgnoreCase("import")) {
			throw new InvalidClassStringException();
		}
		
		if (! it.hasNext()) {
			throw new InvalidClassStringException();
		}
		
		return it.next();
	}
	
	
	
	public GlassClassLoader (){
		super();
	}
	
	
	
	public Set<GlassClass> loadImports (final HTMLDocument document){
		final Set<GlassClass> classList;
		
		classList = new HashSet<>();
		
		for (final var element : document.getChildren()) {
			if (element instanceof HTMLSpecialElement) try {
				final GlassClass classObject;
				final String     classString;
				
				classString = extractClassString(element.getContent());
				
				classObject = new GlassClass(classString);
				
				classList.add(classObject);
			} catch (final InvalidClassStringException ex) {
				Exceptions.ignore(ex);
			}
		}
		
		return classList;
	}
}