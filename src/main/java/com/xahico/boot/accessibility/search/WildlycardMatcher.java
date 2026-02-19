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
public final class WildlycardMatcher extends Matcher {
	private final String  pattern;
	private final boolean required;
	
	
	
	WildlycardMatcher (final String pattern, final boolean required){
		super();
		
		this.pattern = pattern;
		this.required = required;
	}
	
	
	
	@Override
	public boolean hasKey (){
		return false;
	}
	
	@Override
	public boolean isAnti (){
		return false;
	}
	
	@Override
	public boolean isRequired (){
		return this.required;
	}
	
	@Override
	public String key (){
		throw new UnsupportedOperationException("Not supported.");
	}
	
	@Override
	public double match (final String text){
		if (null != text) {
			return RatingHelpers.rate(this.pattern, text);
		} else {
			return 0.0;
		}
	}
	
	@Override
	public String pattern (){
		return this.pattern;
	}
	
	@Override
	public String[] patternList (){
		return new String[]{this.pattern};
	}
	
	@Override
	public String toString (){
		return ("ANY MATCH '" + this.pattern + "'");
	}
}