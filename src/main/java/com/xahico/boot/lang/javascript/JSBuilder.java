/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.lang.javascript;

import com.xahico.boot.dev.Helper;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class JSBuilder {
	private final StringBuilder sb = new StringBuilder();
	
	
	
	public JSBuilder (){
		super();
	}
	
	
	
	public JSBuilder add (final JSNamedObject object){
		sb.append(object.toJavaScript(0));
		sb.append("\n");
		
		return this;
	}
	
	@Helper
	public JSBuilder addLine (){
		return this.addLine("");
	}
	
	public JSBuilder addLine (final String line){
		sb.append(line);
		sb.append("\n");
		
		return this;
	}
	
	public JSCode build (){
		return new JSCode(sb.toString());
	}
	
	public String buildString (){
		return this.build().toJavaScript();
	}
	
	public boolean isEmpty (){
		return sb.isEmpty();
	}
}