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
public class GWXAccessException extends GWXException {
	public GWXAccessException (final String msg){
		super(GWXStatus.AUTHORIZATION, msg);
	}
}