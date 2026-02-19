/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.xahico.boot.lang.jsox;

import com.xahico.boot.util.Parameters;
import org.json.JSONObject;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public interface JSOX {
	default void assume (final JSOX other){
		this.assume(other, true);
	}
	
	void assume (final JSOX other, final boolean reset);
	
	default void assume (final String data){
		this.assume(data, true);
	}
	
	void assume (final String data, final boolean reset);
	
	Object copy ();
	
	void copyTo (final JSOX other);
	
	Object get (final String key);
	
	JSONObject json ();
	
	void json (final JSONObject json);
	
	Parameters parameterize ();
	
	String toJSONString ();
	
	String toJSONStringCompact ();
}