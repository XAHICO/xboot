/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.synchronicity;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public enum ClockedIntensity {
	HIGH(1, 100, 100),
	
	LOW(100, 500, 1000),
	
	MEDIUM(10, 500, 1000),
	
	VERY_HIGH(1, 0, 10);
	
	
	
	private final long waitBase;
	private final long waitIdle;
	private final long waitIncrement;
	
	
	
	ClockedIntensity (final long waitBase, final long waitIncrement, final long waitIdle){
		this.waitBase = waitBase;
		this.waitIncrement = waitIncrement;
		this.waitIdle = waitIdle;
	}
	
	
	
	public long activeClock (final int cores){
		return (this.waitBase + (long)(((double)this.waitIncrement) / ((double)cores)));
	}
	
	public long idleClock (){
		return this.waitIdle;
	}
}