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
public class HTMLSpecialElement extends HTMLElement {
	public HTMLSpecialElement (){
		super();
	}
	
	
	
	@Override
	public HTMLElement duplicate (){
		final HTMLSpecialElement clone;
		
		clone = new HTMLSpecialElement();
		clone.setContent(this.getContent());
		
		return clone;
	}
	
	@Override
	public String toHTMLString (final int depth, final boolean safe){
		final StringBuilder sb;
		
		sb = new StringBuilder();
		
		if (depth > 0) {
			sb.append(LTAB.repeat(depth));
		}
		
		sb.append("<?");
		sb.append(this.getContent());
		sb.append("?>");
		
		return sb.toString();
	}
}