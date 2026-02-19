/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.lang.xml;

import java.util.Collection;
import java.util.Iterator;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class XMLStringBuilder {
	private int                 depth = 0;
	private final StringBuilder stringBuilder = new StringBuilder();
	
	
	
	public XMLStringBuilder (){
		super();
	}
	
	
	
	public XMLStringBuilder attribute (final String key, final boolean value){
		return this.attribute(key, Boolean.toString(value));
	}
	
	public XMLStringBuilder attribute (final String key, final int value){
		return this.attribute(key, Integer.toString(value));
	}
	
	public XMLStringBuilder attribute (final String key, final String value){
		stringBuilder.append(" ");
		stringBuilder.append(key);
		stringBuilder.append("=");
		stringBuilder.append("\"");
		stringBuilder.append(XMLUtilities.translateStringToXML(value));
		stringBuilder.append("\"");
		
		return XMLStringBuilder.this;
	}
	
	public XMLStringBuilder element (final XMLObject element){
		if (! isElementClosed()) {
			elementClose();
		}
		
		stringBuilder.append(element.toXML(depth + 1));
		
		this.line();
		this.tabulate();
		
		return XMLStringBuilder.this;
	}
	
	public XMLStringBuilder elementBegin (final String element){
		this.tabulate();
		
		stringBuilder.append("<");
		stringBuilder.append(element);
		
		return XMLStringBuilder.this;
	}
	
	private XMLStringBuilder elementClose (){
		stringBuilder.append(">");
		
		this.line();
		
		return XMLStringBuilder.this;
	}
	
	public XMLStringBuilder elementEnd (final String element){
		if (stringBuilder.charAt(stringBuilder.length() - 1) == '\t') {
			// Has children.
			stringBuilder.append("<");
			stringBuilder.append("/");
			stringBuilder.append(element);
			stringBuilder.append(">");
		} else {
			// Has no children.
			stringBuilder.append("/");
			stringBuilder.append(">");
		}
		
		return XMLStringBuilder.this;
	}
	
	public XMLStringBuilder elements (final Collection<? extends XMLObject> elements){
		final Iterator<? extends XMLObject> it;
		
		if (! elements.isEmpty()) {
			if (! isElementClosed()) {
				elementClose();
			}

			it = elements.iterator();

			while (it.hasNext()) {
				final XMLObject element;

				element = it.next();

				stringBuilder.append(element.toXML(depth + 1));
				
				this.line();
			}
			
			this.tabulate();
		}
		
		return XMLStringBuilder.this;
	}
	
	private boolean isElementClosed (){
		return (stringBuilder.charAt(stringBuilder.length() - 1) == '>');
	}
	
	private XMLStringBuilder line (){
		stringBuilder.append("\n");
		
		return XMLStringBuilder.this;
	}
	
	public void setDepth (final int depth){
		this.depth = depth;
	}
	
	public XMLStringBuilder tabulate (){
		stringBuilder.append("\t".repeat(depth));
		
		return XMLStringBuilder.this;
	}
	
	@Override
	public String toString (){
		return stringBuilder.toString();
	}
}