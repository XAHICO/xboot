/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.xahico.boot.lang.json;

import org.json.JSONObject;

/**
 * ...
 * 
 * @param <T> 
 * ...
 * 
 * @author Tuomas Kontiainen
**/
@FunctionalInterface
public interface JSONConstructor <T extends JSONSerializable> {
	T newInstance (final JSONObject json);
}