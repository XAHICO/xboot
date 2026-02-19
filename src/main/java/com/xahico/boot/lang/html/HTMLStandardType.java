/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.lang.html;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public enum HTMLStandardType {
	A(false, false),
	BODY(true, false),
	BUTTON(false, false),
	DIV(false, false),
	H1(false, false),
	H2(false, false),
	H3(false, false),
	H4(false, false),
	HEAD(true, false),
	HR(false, false),
	HTML(true, false),
	I(false, false),
	INPUT(false, false),
	LABEL(false, false),
	LINK(false, false),
	META(false, false),
	P(false, false),
	PROGRESS(false, false),
	SCRIPT(false, true),
	STYLE(false, true),
	TABLE(true, false),
	TBODY(true, false),
	TD(true, false),
	TEXT(false, false),
	THEAD(true, false),
	TITLE(true, false),
	TR(true, false);
	
	
	
	public static HTMLStandardType parseString (final String string){
		for (final HTMLStandardType type : HTMLStandardType.values()) {
			if (type.name().equalsIgnoreCase(string)) {
				return type;
			}
		}
		
		return null;
	}
	
	
	
	private final boolean canContainElements;
	private final boolean withLanguageContent;
	
	
	
	HTMLStandardType (final boolean canContainElements, final boolean withLanguageContent){
		this.canContainElements = canContainElements;
		this.withLanguageContent = withLanguageContent;
	}
	
	
	
	public boolean canContainElements (){
		return this.canContainElements;
	}
	
	public String toTypeString (){
		return this.name().toLowerCase();
	}
	
	public boolean withLanguageContent (){
		return this.withLanguageContent;
	}
}