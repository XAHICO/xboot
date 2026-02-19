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
public enum Score {
	ACCEPTABLE(1, 0.333),
	BAD(0, 0.0),
	EXCELLENT(3, 0.799),
	GOOD(2, 0.547),
	PERFECT(4, 0.987);
	
	
	
	public static Score get (final double rating){
		if (rating >= PERFECT.minimumRating()) 
			return PERFECT;
		
		if (rating >= EXCELLENT.minimumRating()) 
			return EXCELLENT;
		
		if (rating >= GOOD.minimumRating()) 
			return GOOD;
		
		if (rating >= ACCEPTABLE.minimumRating()) 
			return ACCEPTABLE;
		
		return Score.BAD;
	}
	
	
	
	private final double minimumRating;
	private final int    value;
	
	
	
	Score (final int value, final double minimumRating){
		this.value = value;
		this.minimumRating = minimumRating;
	}
	
	
	
	public double minimumRating (){
		return this.minimumRating;
	}
	
	public int value (){
		return this.value;
	}
}