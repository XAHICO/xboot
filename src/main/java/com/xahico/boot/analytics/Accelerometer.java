/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.xahico.boot.analytics;

/**
 * TBD.
 * 
 * @author KARBAROTTA
**/
public interface Accelerometer {
	Accelerometer feed (final int num);
	
	Accelerometer feed (final int... nums);
	
	int max ();
	
	Accelerometer max (final int newMax);
	
	int mean ();
	
	default Accelerometer reduce (){
		this.reduceTo(this.max());
		
		return Accelerometer.this;
	}
	
	Accelerometer reduceTo (final int count);
	
	Accelerometer reset ();
	
	int values ();
}