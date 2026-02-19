/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.util.async;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class Timer {
	private long lastTick = -1;
	private long tickInterval = -1;
	private int  ticks = 0;
	
	
	
	public Timer (){
		super();
	}
	
	
	
	public boolean ready (){
		return ((System.currentTimeMillis() - this.lastTick) >= this.tickInterval);
	}
	
	public void reset (){
		this.lastTick = -1;
		this.tickInterval = -1;
		this.ticks = 0;
	}
	
	public void setTickInterval (final long timeMillis){
		this.tickInterval = timeMillis;
	}
	
	public boolean tick (){
		final long timeNow;
		
		timeNow = System.currentTimeMillis();
		
		if ((timeNow - this.lastTick) < this.tickInterval) 
			return false;
		else {
			this.lastTick = timeNow;
			this.ticks++;
			
			return true;
		}
	}
	
	public int ticks (){
		return this.ticks;
	}
}