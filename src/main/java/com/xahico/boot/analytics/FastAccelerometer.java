/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.xahico.boot.analytics;

import com.xahico.boot.analytics.Accelerometer;

/**
 * TBD.
 * A predictive implementation of the {@code Accelerator} class. 
 * Results are guaranteed not to be absolute but are computed very fast and with minimal memory 
 * consuption as compared to the reliable {@link #SteadyAccelerator} class. 
 * Any results are likely to be very misleading with minimal wide-ranging values, 
 * but far less so the other way around.
 * 
 * @author KARBAROTTA
**/
public class FastAccelerometer implements Accelerometer {
	private int max = Integer.MAX_VALUE;
	private int total = 0;
	private int values = 0;
	
	
	
	public FastAccelerometer (){
		super();
	}
	
	
	
	@Override
	public FastAccelerometer feed (final int num){
		if (this.values() < this.max()) {
			this.values++;
		} else {
			this.total -= (this.total / this.values);
		}
		
		this.total += num;
		
		return FastAccelerometer.this;
	}
	
	@Override
	public FastAccelerometer feed (final int... nums){
		for (final var num : nums) {
			this.feed(num);
		}
		
		return FastAccelerometer.this;
	}
	
	@Override
	public int max (){
		return this.max;
	}
	
	@Override
	public FastAccelerometer max (final int newMax){
		this.max = newMax;
		
		return FastAccelerometer.this;
	}
	
	@Override
	public int mean (){
		return (this.total / this.values());
	}
	
	@Override
	public FastAccelerometer reduceTo (final int count){
		this.values = count;
		
		return FastAccelerometer.this;
	}
	
	@Override
	public FastAccelerometer reset (){
		this.total = this.values = 0;
		
		return FastAccelerometer.this;
	}
	
	@Override
	public int values (){
		return this.values;
	}
}