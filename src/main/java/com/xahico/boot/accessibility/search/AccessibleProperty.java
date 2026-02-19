/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.xahico.boot.accessibility.search;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public interface AccessibleProperty {
	String get ();
	
	String key ();
	
	default double match (final Matcher matcher){
		return matcher.match(this.get());
	}
	
	default ScoreMultiplier scoreMultiplier (){
		return ScoreMultiplier.NORMAL;
	}
}