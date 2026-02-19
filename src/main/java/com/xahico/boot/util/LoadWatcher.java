/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */

package com.xahico.boot.util;

/**
 * TBD.
 * 
 * @param <T> 
 * ...
 * 
 * @author root-user
**/
public interface LoadWatcher <T> {
	void begin (final int total);
	
	void end ();
	
	void next (final int index, final int total, final T object);
}