/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.lang.xml;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public abstract class XMLObject {
	public XMLObject (){
		super();
	}
	
	
	
	public final String toXML (){
		return this.toXML(0);
	}
	
	public abstract String toXML (final int depth);
}