/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.accessibility.search;

import java.util.List;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class SpecifiedMatcher extends Matcher {
	private final boolean      anti;
	private final String       key;
	private final boolean      required;
	private final List<String> valueList;
	
	
	
	SpecifiedMatcher (final String key, final List<String> valueList, final boolean anti, final boolean required){
		super();
		
		this.key = key;
		this.valueList = valueList;
		this.anti = anti;
		this.required = required;
	}
	
	
	
	@Override
	public boolean hasKey (){
		return true;
	}
	
	@Override
	public boolean isAnti (){
		return this.anti;
	}
	
	public boolean isMultiOption (){
		return (this.valueList.size() > 1);
	}
	
	@Override
	public boolean isRequired (){
		return this.required;
	}
	
	@Override
	public String key (){
		return this.key;
	}
	
	@Override
	public double match (final String text){
		if (this.required) {
			if (this.anti) {
				for (final var value : this.valueList) {
					if (text.contains(value)) {
						return 0.0;
					}
				}

				return 1.0;
			} else {
				for (final var value : this.valueList) {
					if (text.contains(value)) {
						return 1.0;
					}
				}

				return 0.0;
			}
		} else {
			double highScore = 0.0;

			for (final var value : this.valueList) {
				final double score;

				score = RatingHelpers.eval(RatingHelpers.rate(value, text), this.anti);

				if (score > highScore) {
					highScore = score;
				}
			}

			return highScore;
		}
	}
	
	@Override
	public String pattern (){
		return AccessibilityUtilities.listToReadableString(this.valueList);
	}
	
	@Override
	public String[] patternList (){
		return this.valueList.toArray(new String[this.valueList.size()]);
	}
	
	@Override
	public String toString (){
		final StringBuilder sb;
		
		sb = new StringBuilder();
		
		if (this.anti) {
			sb.append("NOT");
			sb.append(' ');
		}
		
		sb.append("SET");
		sb.append(' ');
		sb.append('$');
		sb.append('(');
		sb.append(this.key);
		sb.append(')');
		sb.append(' ');
		sb.append("MATCH");
		sb.append(' ');
		
		for (var i = 0; i < this.valueList.size(); i++) {
			sb.append('\'');
			sb.append(this.valueList.get(i));
			sb.append('\'');
			
			if ((i + 1) < this.valueList.size()) {
				sb.append(' ');
				sb.append("OR");
				sb.append(' ');
			}
		}
		
		return sb.toString();
	}
}