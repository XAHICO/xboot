/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.lang.javascript;

import com.xahico.boot.util.StringUtilities;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class JSEnum extends JSNamedObject {
	private String       name = null;
	private List<String> values = new ArrayList<>();
	
	
	
	public JSEnum (){
		super();
	}
	
	
	
	public JSEnum addValue (final String value){
		this.values.add(value);
		
		return this;
	}
	
	public JSEnum addValues (final String... values){
		this.values.addAll(Arrays.asList(values));
		
		return this;
	}
	
	@Override
	public String getName (){
		return this.name;
	}
	
	public JSEnum setName (final String name){
		this.name = name;
		
		return this;
	}
	
	@Override
	public String toJavaScript (final int depth){
		final StringBuilder sb;
		
		sb = new StringBuilder();
		
		if (depth > 0) {
			sb.append(LTAB.repeat(depth));
		}
		
		sb.append(MODIFIER_CONST);
		sb.append(" ");
		sb.append(this.name);
		sb.append(" ");
		sb.append("=");
		sb.append(" ");
		sb.append("{");
		sb.append(LINE);
		
		for (var i = 0; i < this.values.size(); i++) {
			sb.append(LTAB.repeat(depth + 1));
			sb.append(this.values.get(i));
			sb.append(":");
			sb.append(" ");
			sb.append(StringUtilities.quote(this.values.get(i)));
			
			if ((i + 1) < this.values.size()) {
				sb.append(",");
			}
			
			sb.append(LINE);
		}
		
		sb.append("}");
		sb.append(";");
		
		return sb.toString();
	}
}