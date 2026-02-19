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
public final class HTMLUtilities {
	private static final int TABBING_EQUIVALENT_SPACES = 4;
	
	
	
	public static int calculatePaddingSpaces (final String line){
		int padding = 0;
		
		for (var i = 0; i < line.length(); i++) {
			final char c;
			
			c = line.charAt(i);
			
			if (c == '\t') 
				padding += TABBING_EQUIVALENT_SPACES;
			else if (c == ' ') 
				padding += 1;
			else {
				break;
			}
		}
		
		return padding;
	}
	
	public static int calculatePaddingTabs (final String line){
		return ((int)Math.round(((double)calculatePaddingSpaces(line)) / ((double)TABBING_EQUIVALENT_SPACES)));
	}
	
	public static HTMLNode createMeta (final String name, final String content){
		final HTMLNode script;
		
		script = new HTMLNode(HTMLStandardType.META);
		script.setAttribute("content", content);
		script.setAttribute("name", name);
		
		return script;
	}
	
	public static HTMLNode createScript (final String content){
		final HTMLNode script;
		
		script = new HTMLNode(HTMLStandardType.SCRIPT);
		script.setContent(content);
		
		return script;
	}
	
	
	
	private HTMLUtilities (){
		throw new UnsupportedOperationException("Not supported.");
	}
}