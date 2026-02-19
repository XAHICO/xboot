/*
 * Written by Tuomas Kontiainen <https://www.github.com/tutomiko>
 * 
 * Copyright (c) 2023, Tuomas Kontiainen
 */
package com.xahico.boot.lang.json;

import org.json.JSONObject;

/**
 * ...
 * 
 * @author Tuomas Kontiainen
**/
public interface JSONSerializable {
	/**
	 * Creates a JSON object from this object.
	 * 
	 * @return 
	 * A JSON object representing this object.
	**/
	JSONObject json ();
	
	/**
	 * Fills this object from a given JSON object's values.
	 * 
	 * @param json 
	 * JSON object to read.
	**/
	void json (final JSONObject json);
}