/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.net.sock.model.lobex;

import com.xahico.boot.lang.jsox.JSOXObject;
import com.xahico.boot.lang.jsox.JSOXVariant;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class LOBEXPacket extends JSOXObject {
	public String      control;
	public JSOXVariant data;
	public int         id;
	public boolean     status;
	public Type        type;
	
	
	
	public static enum Type {
		EVENT,
		TRANSACTION,
	}
}