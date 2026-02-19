/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.lang.javascript;

import com.xahico.boot.util.StringUtilities;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public class JSVariable extends JSNamedObject {
	private String name = null;
	private JSType type = null;
	private String value = null;
	
	
	
	public JSVariable (){
		super();
	}
	
	
	
	public String getName (){
		return this.name;
	}
	
	public void setName (final String name){
		this.name = name;
	}
	
	public void setType (final JSType type){
		this.type = type;
	}
	
	public void setValue (final String value){
		this.value = value;
	}
	
	@Override
	public String toJavaScript (final int depth){
		final StringBuilder sb;
		
		sb = new StringBuilder();
		
		if (depth > 0) {
			sb.append(LTAB.repeat(depth));
		}
		
		sb.append("var");
		sb.append(" ");
		sb.append(this.name);
		sb.append(" ");
		sb.append("=");
		sb.append(" ");
		
		if (null == this.value) 
			sb.append("null");
		else switch (this.type) {
			case DICTIONARY:
				sb.append(this.value);
				
				break;
			case NUMBER:
				sb.append(this.value);
				
				break;
			case OBJECT:
				sb.append(this.value);
				
				break;
			case STRING:
				sb.append(StringUtilities.quote(this.value));
				
				break;
			default: {
				break;
			}
		}
		
		sb.append(";");
		
		return sb.toString();
	}
}