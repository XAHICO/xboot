/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.publish;

import com.xahico.boot.lang.jsox.JSOXObject;
import com.xahico.boot.lang.jsox.JSOXVariant;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class GWXEventObject extends JSOXObject {
	public JSOXVariant data;
	public String      id;
	public String      source;
	public String      target;
	public long        timestamp;
	
	
	
	GWXEventObject (){
		super();
	}
}