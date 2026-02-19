/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.net.web.http.model.glass;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
final class GlassClass {
	private final String string;
	
	
	
	GlassClass (final String string){
		super();
		
		this.string = string;
	}
	
	
	
	public String name (){
		final int delimiter;
		
		delimiter = this.string.lastIndexOf('.');
		
		if (delimiter == -1) 
			return "";
		
		return this.string.substring(delimiter + 1);
	}
	
	public String path (){
		return this.string.replace('.', '/');
	}
	
	@Override
	public String toString (){
		return this.string;
	}
}