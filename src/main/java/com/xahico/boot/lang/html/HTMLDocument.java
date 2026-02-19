/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.lang.html;

import java.io.IOException;
import java.util.Iterator;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public class HTMLDocument extends HTMLNode {
	public static final String DOCUMENT_TAG = "!DOCTYPE";
	
	
	
	private boolean includeDef = true;
	
	
	
	public HTMLDocument (){
		super();
	}
	
	
	
	public void assertHeadBody (){
		assert((null != this.lookupFirst(HTMLStandardType.HEAD, -1)) && (null != this.lookupFirst(HTMLStandardType.BODY, -1)));
	}
	
	@Override
	public HTMLDocument duplicate (){
		try {
			return HTMLParser.parseString(this.toHTMLString());
		} catch (final HTMLException | IOException ex) {
			throw new Error(ex);
		}
	}
	
	public HTMLNode getDocumentRoot (){
		final HTMLElement rootNode;
		
		if (! this.hasChildren()) 
			return null;
		
		rootNode = this.getChild(0);
		
		if (rootNode instanceof HTMLNode) 
			return (HTMLNode)(rootNode);
		else {
			return null;
		}
	}
	
	@Override
	public boolean isCustomType (){
		return false;
	}
	
	public boolean isIncludeDef (){
		return this.includeDef;
	}
	
	@Override
	public boolean isStandardType (){
		return false;
	}
	
	public HTMLDocument removeSpecialElements (){
		final Iterator<HTMLElement> it;
		
		it = this.getChildren().iterator();
		
		while (it.hasNext()) {
			final HTMLElement element;
			
			element = it.next();
			
			if (element instanceof HTMLSpecialElement) {
				it.remove();
			}
		}
		
		return HTMLDocument.this;
	}
	
	public void setIncludeDef (final boolean include){
		this.includeDef = include;
	}
	
	@Override
	public String toHTMLString (final int depth, final boolean safe){
		final StringBuilder sb;
		
		sb = new StringBuilder();
		
		if (this.isIncludeDef()) {
			if (depth > 0) {
				sb.append(LTAB.repeat(depth));
			}

			sb.append("<");
			sb.append(DOCUMENT_TAG);
			sb.append(" ");
			sb.append("HTML");
			sb.append(">");

			if (depth != -1) {
				sb.append(LINE);
				sb.append(LINE);
			}
		}
		
		for (final var childElement : this.getChildren()) {
			sb.append(childElement.toHTMLString(depth, safe));
			
			if (depth != -1) {
				sb.append(LINE);
			}
		}
		
		return sb.toString();
	}
	
	public String toHTMLStringHumanUnreadable (){
		return this.toHTMLString(-1, true);
	}
}