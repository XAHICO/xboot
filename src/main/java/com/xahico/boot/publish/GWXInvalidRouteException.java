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
public class GWXInvalidRouteException extends GWXException {
	public GWXInvalidRouteException (final String msg){
		super(GWXStatus.INVALID_LOCATION, msg);
	}
}