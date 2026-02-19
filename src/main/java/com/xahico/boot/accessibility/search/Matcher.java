/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.accessibility.search;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public abstract class Matcher {
	Matcher (){
		super();
	}
	
	
	
	public abstract boolean hasKey ();
	
	public abstract boolean isAnti ();
	
	public abstract boolean isRequired ();
	
	public abstract String key ();
	
	public abstract double match (final String text);
	
	public abstract String pattern ();
	
	public abstract String[] patternList ();
}