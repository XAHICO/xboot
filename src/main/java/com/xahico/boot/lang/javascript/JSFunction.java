/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.lang.javascript;

import com.xahico.boot.dev.Helper;
import java.util.LinkedList;
import java.util.List;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class JSFunction extends JSNamedObject {
	private JSCode                  body = null;
	private String                  name = null;
	private final List<JSParameter> parameters = new LinkedList<>();
	
	
	
	
	public JSFunction (){
		super();
	}
	
	
	
	public JSFunction addParameter (final JSParameter parameter){
		this.parameters.add(parameter);
		
		return this;
	}
	
	@Helper
	public JSFunction addParameter (final String name){
		return this.addParameter(name, null);
	}
	
	@Helper
	public JSFunction addParameter (final String name, final String defaultValue){
		final JSParameter parameter;
		
		parameter = new JSParameter();
		parameter.setDefaultValue(defaultValue);
		parameter.setName(name);
		
		return this.addParameter(parameter);
	}
	
	public String getName (){
		return this.name;
	}
	
	public JSFunction setBody (final JSCode body){
		this.body = body;
		
		return this;
	}
	
	public JSFunction setName (final String name){
		this.name = name;
		
		return this;
	}
	
	@Override
	public String toJavaScript (final int depth){
		final StringBuilder sb;
		
		sb = new StringBuilder();
		
		if ((depth > 0) && (null != this.name)) {
			sb.append(LTAB.repeat(depth));
		}
		
		sb.append("function");
		sb.append(" ");
		
		if (null != this.name) {
			sb.append(this.name);
			sb.append(" ");
		}
		
		sb.append("(");
		
		for (var i = 0; i < this.parameters.size(); i++) {
			final JSParameter parameter;
			
			parameter = this.parameters.get(i);
			
			sb.append(parameter.getName());
			
			if (null != parameter.getDefaultValue()) {
				sb.append(" ");
				sb.append("=");
				sb.append(" ");
				sb.append(parameter.getDefaultValue());
			}
			
			if ((i + 1) < this.parameters.size()) {
				sb.append(",");
				sb.append(" ");
			}
		}
		
		sb.append(")");
		sb.append("{");
		sb.append(LINE);
		sb.append(this.body.toJavaScript(depth + 1));
		sb.append(LINE);
		sb.append(LTAB.repeat(depth));
		sb.append("}");
		
		return sb.toString();
	}
}