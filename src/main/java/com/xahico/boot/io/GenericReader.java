/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.io;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class GenericReader {
	private long position = 0;
	private long size = 0;
	
	
	
	public GenericReader (){
		super();
	}
	
	
	
	public boolean isBegin (){
		return (this.position == 0);
	}
	
	public boolean isEnd (){
		return (this.position == this.size);
	}
	
	public long position (){
		return this.position;
	}
	
	public void position (final long position){
		this.position = position;
	}
	
	public long size (){
		return this.size;
	}
	
	public void size (final long size){
		this.size = size;
	}
}