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
public final class GenericReadResults {
	private long    cursor = 0;
	private boolean eof = false;
	
	
	
	public GenericReadResults (){
		super();
	}
	
	
	
	public long cursor (){
		return this.cursor;
	}
	
	public void cursor (final long cursor){
		this.cursor = cursor;
	}
	
	public boolean isEOF (){
		return this.eof;
	}
	
	public void markEOF (final boolean eof){
		this.eof = eof;
	}
}