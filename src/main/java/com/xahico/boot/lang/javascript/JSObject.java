/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.lang.javascript;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public abstract class JSObject {
	protected static final String LINE = "\n";
	protected static final String LTAB = "\t";
	
	public static final String MODIFIER_CONST = "const";
	
	
	
	public JSObject (){
		super();
	}
	
	
	
	public String toJavaScript (){
		return this.toJavaScript(0);
	}
	
	public abstract String toJavaScript (final int depth);
	
	@Override
	public String toString (){
		return this.toJavaScript(0);
	}
}