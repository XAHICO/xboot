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
public class GWXParameterException extends GWXException {
	public GWXParameterException (final String msg){
		super(GWXStatus.INVALID_PARAMETERS, msg);
	}
}