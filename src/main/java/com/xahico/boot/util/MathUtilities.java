/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.util;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class MathUtilities {
	public static double floor (final double value, final int precision){
		final int scale;
		
		scale = (int) Math.pow(10, precision);
		
		return (double)(Math.floor(value * scale) / scale);
	}
	
	public static double round (final double value, final int precision){
		final int scale;
		
		scale = (int) Math.pow(10, precision);
		
		return (double)(Math.round(value * scale) / scale);
	}
	
	public static int sigma (final int n){
		int x = 0;
		
		for (var i = 1; i != (n + 1); i++) {
			x += i;
		}
		
		return x;
	}
	
	
	
	private MathUtilities (){
		throw new UnsupportedOperationException("Not supported.");
	}
}