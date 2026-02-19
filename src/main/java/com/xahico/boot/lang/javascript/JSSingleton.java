/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.lang.javascript;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class JSSingleton extends JSNamedObject {
	private JSCode                      body = null;
	private final Map<String, JSNamedObject> exports = new HashMap<>();
	private String                      name = null;
	
	
	
	public JSSingleton (){
		super();
	}
	
	
	
	public JSSingleton addExport (final String key, final JSNamedObject value){
		this.exports.put(key, value);
		
		return this;
	}
	
	public String getName (){
		return this.name;
	}
	
	public JSSingleton setBody (final JSCode body){
		this.body = body;
		
		return this;
	}
	
	public JSSingleton setName (final String name){
		this.name = name;
		
		return this;
	}
	
	@Override
	public String toJavaScript (final int depth){
		final Iterator<String> it;
		final StringBuilder    sb;
		
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
		sb.append("(");
		sb.append("function");
		sb.append("(");
		sb.append(")");
		sb.append(" ");
		sb.append("{");
		sb.append(LINE);
		
		if (null != this.body) {
			sb.append(this.body.toJavaScript(depth + 1));
			sb.append(LINE);
			sb.append(LINE);
		}
		
		sb.append(LTAB.repeat(depth + 1));
		sb.append("return");
		sb.append(" ");
		sb.append("{");
		sb.append(LINE);
		
		it = this.exports.keySet().iterator();
		
		while (it.hasNext()) {
			final String export;
			
			export = it.next();
			
			sb.append(LTAB.repeat(depth + 2));
			sb.append(export);
			sb.append(":");
			sb.append(" ");
			sb.append(this.exports.get(export).toJavaScript(depth + 2));
			
			if (it.hasNext()) {
				sb.append(",");
				sb.append(LINE);
			}
		}
		
		sb.append(LINE);
		sb.append(LTAB.repeat(depth + 1));
		sb.append("}");
		sb.append(";");
		sb.append(LINE);
		sb.append("}");
		sb.append(")");
		sb.append("(");
		sb.append(")");
		sb.append(";");
		
		return sb.toString();
	}
}