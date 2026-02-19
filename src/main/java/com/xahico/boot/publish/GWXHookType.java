/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.publish;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public enum GWXHookType {
	AUTH,
	DESTROY,
	IDLE,
	WAKE;
	
	
	
	public static GWXHookType parseString (final String string){
		for (final var hookType : GWXHookType.values()) {
			if (hookType.name().equalsIgnoreCase(string)) {
				return hookType;
			}
		}
		
		return null;
	}
}