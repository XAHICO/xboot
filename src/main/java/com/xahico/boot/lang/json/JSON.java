/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xahico.boot.lang.json;

import org.json.JSONObject;

/**
 * TBD.
 * 
 * @author Tuomas Kontiainen
**/
public final class JSON {
	public static JSONObject newObject (){
		return new JSONObject();
	}
	
	public static JSONObject newObject (final JSONObject other){
		return JSON.newObject(other.toString());
	}
	
	public static JSONObject newObject (final String data){
		final JSONObject object;
		
		if (data.isEmpty() || data.isBlank()) 
			object = new JSONObject();
		else {
			object = new JSONObject(data);
			
			if (JSONUtilities.containsUnstructuredArrays(object)) {
				return JSONUtilities.compileUnstructuredArrays(object);
			}
		}
		
		return object;
	}
	
	
	
	private JSON (){
		throw new UnsupportedOperationException("Not supported.");
	}
}