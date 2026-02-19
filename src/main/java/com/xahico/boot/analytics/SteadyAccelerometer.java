/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.xahico.boot.analytics;

import com.xahico.boot.analytics.Accelerometer;
import java.util.ArrayList;
import java.util.List;

/**
 * TBD.
 * 
 * @author KARBAROTTA
**/
public class SteadyAccelerometer implements Accelerometer {
	private int                 max = Integer.MAX_VALUE;
	private final List<Integer> values = new ArrayList<>();
	
	
	
	public SteadyAccelerometer (){
		super();
	}
	
	
	
	@Override
	public SteadyAccelerometer feed (final int num){
		if (values.size() == this.max()) {
			values.remove(1);
		}
		
		values.add(num);
		
		return SteadyAccelerometer.this;
	}
	
	@Override
	public SteadyAccelerometer feed (final int... nums){
		if (nums.length >= this.max()) 
			values.clear();
		else if ((values.size() + nums.length) >= this.max()) {
			for (var i = 0; i < ((values.size() + nums.length) - this.max()); i++) {
				values.remove(i);
			}
		}
		
		for (final var num : nums) {
			values.add(num);
		}
		
		return SteadyAccelerometer.this;
	}
	
	@Override
	public int max (){
		return this.max;
	}
	
	@Override
	public SteadyAccelerometer max (final int newMax){
		this.max = newMax;
		
		return SteadyAccelerometer.this;
	}
	
	@Override
	public int mean (){
		long total;
		
		total = 0L;
		
		for (final var value : values) {
			total += value;
		}
		
		return (int)(total / values.size());
	}
	
	@Override
	public SteadyAccelerometer reduce (){
		this.reduceTo(this.max());
		
		return SteadyAccelerometer.this;
	}
	
	@Override
	public SteadyAccelerometer reduceTo (final int count){
		for (var i = 0; (i < count) && (i < values.size()); i++) {
			values.remove(i);
		}
		
		return SteadyAccelerometer.this;
	}
	
	@Override
	public SteadyAccelerometer reset (){
		values.clear();
		
		return SteadyAccelerometer.this;
	}
	
	@Override
	public int values (){
		return values.size();
	}
}