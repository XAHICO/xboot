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
public enum ScoreMultiplier {
	VERY_BIG(3.0),
	BIG(2.0),
	NORMAL(1.0),
	SMALL(0.5),
	VERY_SMALL(0.1);
	
	
	
	private final double value;
	
	
	
	ScoreMultiplier (final double value){
		this.value = value;
	}
	
	
	
	public double value (){
		return this.value;
	}
}