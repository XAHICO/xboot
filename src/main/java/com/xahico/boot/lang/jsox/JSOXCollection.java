/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.xahico.boot.lang.jsox;

import com.xahico.boot.util.Parameters;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public interface JSOXCollection {
	void clear ();
	
	Object copy ();
	
	Parameters parameterize ();
	
	String toJSONString ();
	
	String toJSONStringCompact ();
}