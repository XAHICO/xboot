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
public class HTMLString extends HTMLElement {
	public HTMLString (){
		super();
	}
	
	
	
	@Override
	public HTMLElement duplicate (){
		final HTMLString clone;
		
		clone = new HTMLString();
		clone.setContent(this.getContent());
		
		return clone;
	}
	
	@Override
	public String toHTMLString (final int depth, final boolean safe){
		final StringBuilder sb;
		
		sb = new StringBuilder();
		
		if (depth != -1) {
			sb.append(LTAB.repeat(depth));
		}
		
		sb.append(this.getContent());
		
		return sb.toString();
	}
}