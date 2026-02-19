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
public final class JSParameter {
	private String defaultValue = null;
	private String name = null;
	
	
	
	public JSParameter (){
		super();
	}
	
	
	
	public String getDefaultValue (){
		return this.defaultValue;
	}
	
	public String getName (){
		return this.name;
	}
	
	public void setDefaultValue (final String value){
		this.defaultValue = value;
	}
	
	public void setName (final String name){
		this.name = name;
	}
}